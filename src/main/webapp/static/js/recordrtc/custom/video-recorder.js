class VideoRecorder {

	constructor(videoElement, qualities, config) {
		this.videoElement = videoElement;
		this.qualities = qualities;
		this.quality = qualities[0];
		this.config = config;
		this.avService = new AvService(config);
		this.avUserInterface = new AvUserInterface(videoElement, config);
		this.state = null;
		this.mediaStream = null;
		this.imageCapture = null;
		this.imageCaptureBlob = null;
		this.recorder = null;
		this.blobs = null;
		this.feedWidth = 0;
		this.feedHeight = 0;
		this.endedHandler = null;
		this.timeupdateHandler = null;
		this.oneButtonHandler = null;
	}

	setQualityByName(name) {
		const quality = this.qualities.find(quality => quality.name === name);
		if (quality) {
			this.quality = quality;
		}
	}

	sizeVideoElement(feedWidth = 1280, feedHeight = 720) {
		const browserViewport = jQuery(window);
		const mainContainer = jQuery('#avRecorder');

		const availableWidth = mainContainer.width();
		const viewPortHeightPortion = browserViewport.height() * 0.618;
		const availableHeight = viewPortHeightPortion > 240 ? viewPortHeightPortion : 240;

		const feedAspectRatio = feedWidth / feedHeight;
		const availableAspectRatio = availableWidth / availableHeight;

		let videoWidth;
		let videoHeight;
		if (feedAspectRatio > availableAspectRatio) {
			videoWidth = availableWidth;
			videoHeight = videoWidth / feedAspectRatio;
		} else {
			videoHeight = availableHeight;
			videoWidth = videoHeight * feedAspectRatio;
		}

		const avContainer = jQuery('#avContainer');
		avContainer.width(videoWidth);
		avContainer.height(videoHeight);

		const video = jQuery(this.videoElement);
		video.width(videoWidth);
		video.height(videoHeight);
	}

	mimeType() {
		let mimeType = 'video/webm;codecs=vp8,opus';

		const isSafari = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
		if (isSafari) {
			mimeType = 'video/mp4';
		}

		if (isChrome) {
			//mimeType = 'video/x-matroska;codecs=h264';
		}

		return mimeType;
	}

	stateInit() {
		this.state = RecState.init;
		this.avUserInterface.init();
		this.updateUI();
		this.getMediaStream();
		this.sizeVideoElement();
	}

	stateToWaitingToRecord() {
		if (this.state !== RecState.init) {
			return;
		}

		this.state = RecState.waitingToRecord;
		this.mute();
		this.updateUI();
	}

	handleOneButton() {
		switch (this.state) {
			case RecState.waitingToRecord:
				this.createRecorder();
				if (this.canRecord()) {
					this.state = RecState.recording;
					this.startRecording();
					this.updateUI();
				}
				break;
			case RecState.recording:
				this.state = RecState.stoppedRecording;
				this.stopRecording();
				this.updateUI();
				break;
			case RecState.stoppedRecording:
				this.state = RecState.playing;
				this.startPlaying();
				this.updateUI();
				break;
			case RecState.playing:
				this.state = RecState.stopped;
				this.stopPlaying();
				this.updateUI();
				break;
			case RecState.stopped:
				this.state = RecState.playing;
				this.startPlaying();
				this.updateUI();
				break;
			default:
				break;
		}
	}

	updateUI() {
		const recordingIndicator = jQuery('#recording-container');
		const oneButton = jQuery('#one-button');
		const startRecording = jQuery('#start-recording-symbol').add('#start-recording-text');
		const stopRecording = jQuery('#stop-recording-symbol').add('#stop-recording-text');
		const play = jQuery('#play-symbol').add('#play-text');
		const stop = jQuery('#stop-symbol').add('#stop-text');
		const retryButton = jQuery('#retry-button');
		const confirmButton = jQuery('.o_av_confirm_button');
		const qualityDropdown = jQuery('#o_fiovideo_audio_quality_SELBOX');
		const volumeButton = jQuery('#volume-button');
		const fileSize = jQuery('#file-size');

		const self = this;

		oneButton.blur();

		switch (this.state) {
			case RecState.init:
				this.avUserInterface.hideTimeContainer();
				this.avUserInterface.hideRecordingLengthLimit();
				jQuery(this.videoElement).show();
				recordingIndicator.hide();
				oneButton.hide();
				startRecording.hide();
				stopRecording.hide();
				volumeButton.show();
				play.hide();
				stop.hide();
				retryButton.hide();
				confirmButton.hide();

				jQuery('#time-rail').hide();
				fileSize.hide();

				if (this.oneButtonHandler === null) {
					this.oneButtonHandler = () => {
						self.handleOneButton();
					};
					oneButton.click(this.oneButtonHandler);
				}

				jQuery('#yes-no-dialog').hide();

				this.setQualityByName(qualityDropdown.val());
				qualityDropdown.prop('disabled', true);
				if (this.config.userCanChangeVideoQuality) {
					qualityDropdown.change(() => {
						// A change of this value should not make the form dirty:
						confirmButton.removeClass('o_button_dirty');
						o2c = false;

						self.setQualityByName(qualityDropdown.val());
						self.resetMediaStream();
					});
				} else {
					qualityDropdown.hide();
				}
				break;
			case RecState.waitingToRecord:
				oneButton.show();
				startRecording.show();
				qualityDropdown.prop('disabled', false);
				this.avUserInterface.hideTotalTime();
				fileSize.show();
				this.updateSize(0);
				break;
			case RecState.recording:
				this.avUserInterface.showTimeContainer();
				this.avUserInterface.showRecordingLengthLimitIfApplicable();
				qualityDropdown.prop('disabled', true);
				recordingIndicator.show();
				startRecording.hide();
				stopRecording.show();
				volumeButton.hide();
				break;
			case RecState.stoppedRecording:
				recordingIndicator.hide();
				stopRecording.hide();
				volumeButton.show();
				play.show();
				retryButton.show();
				retryButton.click(() => {
					self.retryDialog();
				});
				retryButton.prop('disabled', false);
				confirmButton.show();
				confirmButton.prop('disabled', false);
				if (this.endedHandler) {
					this.videoElement.removeEventListener('ended', this.endedHandler);
					this.endedHandler = null;
				}
				if (this.timeupdateHandler) {
					this.videoElement.removeEventListener('timeupdate', this.timeupdateHandler);
					this.timeupdateHandler = null;
				}
				break;
			case RecState.playing:
				play.hide();
				stop.show();
				retryButton.prop('disabled', true);
				confirmButton.prop('disabled', true);
				break;
			case RecState.stopped:
				stop.hide();
				play.show();
				retryButton.show();
				retryButton.click(() => {
					self.retryDialog();
				});
				retryButton.prop('disabled', false);
				confirmButton.show();
				confirmButton.prop('disabled', false);
				if (this.endedHandler) {
					this.videoElement.removeEventListener('ended', this.endedHandler);
					this.endedHandler = null;
				}
				if (this.timeupdateHandler) {
					this.videoElement.removeEventListener('timeupdate', this.timeupdateHandler);
					this.timeupdateHandler = null;
				}
				break;
			default:
				break;
		}
	}

	retryDialog() {
		var self = this;

		jQuery('#retry-button').blur();
		jQuery('#yes-no-dialog').show();
		jQuery('#no-button').click(() => {
			jQuery('#yes-no-dialog').hide();
		});
		jQuery('#yes-button').click(() => {
			jQuery('#yes-no-dialog').hide();
			self.retryRecording();
		});
	}

	retryRecording() {
		jQuery('#retry-button').blur();
		if (this.state === RecState.stopped || this.state === RecState.stoppedRecording) {
			this.dispose();
			this.stateInit();
		}
	}

	getMediaStream() {
		const self = this;

		navigator.mediaDevices.getUserMedia({
			audio: {
				echoCancellation: true
			},
			video: {
				height: {ideal: this.quality.height}
			}
		}).then((mediaStream) => {
			const track = mediaStream.getVideoTracks()[0];
			self.imageCapture = new ImageCapture(track);
			const trackSettings = track.getSettings();
			self.feedWidth = trackSettings.width;
			self.feedHeight = trackSettings.height;
			jQuery('.o_video_feed_dimensions').html(`${self.feedWidth} x ${self.feedHeight}`);
			self.sizeVideoElement(trackSettings.width, trackSettings.height);
			self.mediaStream = mediaStream;
			self.mediaStreamReady(mediaStream);
		}).catch((error) => {
			alert('Unable to capture your camera. Please check console logs.');
			console.error(error);
		});
	}

	mediaStreamReady(mediaStream) {
		this.videoElement.srcObject = mediaStream;
		this.mute();
		const self = this;
		setTimeout(() => {
			self.stateToWaitingToRecord();
		}, 1000);
	}

	resetMediaStream() {
		if (this.state !== RecState.waitingToRecord) {
			return;
		}

		this.dispose();
		this.stateInit();
	}

	mute() {
		this.avUserInterface.setVolume(0);
		this.avUserInterface.setMuted();
	}

	createRecorder() {
		if (this.mediaStream === null) {
			console.log('No media stream available');
			return;
		}

		if (this.recorder) {
			console.log('Recorder already created');
			return;
		}

		const self = this;

		this.recorder = new RecordRTC(this.mediaStream, {
			mimeType: this.mimeType(),
			timeSlice: 1000,
			videoBitsPerSecond: this.quality.videoBitsPerSecond,
			audioBitsPerSecond: this.quality.audioBitsPerSecond,
			ondataavailable: (blob) => {
				if (self.blobs === null) {
					self.blobs = [];
				}
				self.blobs.push(blob);
				let size = 0;
				self.blobs.forEach((b) => {
					size += b.size;
				});

				self.avUserInterface.updateTimer();
				self.updateSize(size);

				if (self.config.generatePosterImage && self.blobs.length === 2) {
					self.imageCapture.takePhoto()
						.then((blob) => {
							self.imageCaptureBlob = blob;
							console.log('takePhoto() succeeded');
						})
						.catch((error) => {
							console.log('takePhoto() error: ', error);
						});
				}
			}
		});

		if (this.config.recordingLengthLimit) {
			this.recorder.setRecordingDuration(this.config.recordingLengthLimit, () => {
				self.state = RecState.stoppedRecording;
				self.updateUI();
				self.avUserInterface.updateTotalTime();
				self.stopRecordingCallback();
			});
		}
	}

	updateSize(size) {
		const sizeStr = bytesToSize(size);
		jQuery('#file-size').text(sizeStr);
	}

	canRecord() {
		return this.mediaStream && this.recorder;
	}

	startRecording() {
		if (!this.canRecord()) {
			console.log('No media stream or recorder available');
			return;
		}

		this.mute();

		this.recorder.startRecording();
		this.recorder.camera = this.mediaStream;

		this.avUserInterface.startTimer();
	}

	stopRecording() {
		if (this.recorder === null) {
			console.log('Cannot stop recording. No recorder available.')
			return;
		}
		this.avUserInterface.updateTotalTime();
		const self = this;
		this.recorder.stopRecording(() => {
			self.stopRecordingCallback();
		});
	}

	startPlaying() {
		const self = this;

		if ((this.videoElement.currentTime * 1000 + 100) >= this.avUserInterface.getTotalTimeInMsec()) {
			this.videoElement.currentTime = 0;
			this.avUserInterface.setCurrentTime(0);
		}
		this.videoElement.play();

		this.timeupdateHandler = () => {
			self.avUserInterface.setCurrentTime(self.videoElement.currentTime * 1000);
		};
		this.videoElement.addEventListener('timeupdate', this.timeupdateHandler);

		this.endedHandler = () => {
			self.state = RecState.stopped;
			self.updateUI();
			self.avUserInterface.setCurrentTimeToTotalTime();
		}
		this.videoElement.addEventListener('ended', this.endedHandler);
	}

	stopPlaying() {
		this.videoElement.pause();
	}

	stopRecordingCallback() {
		this.videoElement.src = this.videoElement.srcObject = null;
		this.avUserInterface.setUnmuted();
		this.avUserInterface.setVolume(0.75);
		this.videoElement.src = URL.createObjectURL(this.recorder.getBlob());
		this.videoElement.pause();
		this.videoElement.currentTime = 0;
		this.avUserInterface.setCurrentTime(0);

		this.mediaStream.stop();

		this.avService.storeRecording({recorder: this.recorder});
		if (this.config.generatePosterImage) {
			this.avService.storePosterImage({imageCaptureBlob: this.imageCaptureBlob});
		}

		jQuery('#time-rail').show();
		this.avUserInterface.showTotalTime();
		this.avUserInterface.hideRecordingLengthLimit();
	}

	destroyRecorder() {
		if (this.recorder === null) {
			return;
		}
		this.recorder.destroy();
		this.recorder = null;
	}

	turnOffVideoElement() {
		this.videoElement.src = this.videoElement.srcObject = null;
	}

	dispose() {
		console.log('videoRecorder.dispose()');
		this.blobs = null;
		this.imageCaptureBlob = null;
		this.imageCapture = null;
		this.destroyRecorder();
		this.releaseMediaStream();
	}

	releaseMediaStream() {
		if (this.mediaStream === null) {
			return;
		}

		const tracks = this.mediaStream.getTracks();
		tracks.forEach((track) => {
			console.log('videoRecorder: stop track', track);
			track.stop();
		});

		this.turnOffVideoElement();
	}
}
