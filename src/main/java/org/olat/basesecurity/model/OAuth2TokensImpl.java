/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.basesecurity.model;

import java.util.Calendar;
import java.util.Date;

import org.olat.basesecurity.OAuth2Tokens;

import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * 
 * Initial date: 24 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuth2TokensImpl implements OAuth2Tokens {
	
	private String accessToken;
	private String refreshToken;
	private Integer expiresIn;
	private Object user;
	private Date expirationDate;
	
	public static OAuth2Tokens valueOf(OAuth2AccessToken oauth2AccessToken) {
		OAuth2TokensImpl tokens = new OAuth2TokensImpl();
		tokens.setAccessToken(oauth2AccessToken.getAccessToken());
		tokens.setRefreshToken(oauth2AccessToken.getRefreshToken());
		tokens.setExpiresIn(oauth2AccessToken.getExpiresIn());
		return tokens;
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@Override
	public Integer getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
		if(expiresIn != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, expiresIn.intValue());
			cal.add(Calendar.SECOND, - 30);
			expirationDate = cal.getTime();
		} else {
			expirationDate = null;
		}
	}

	@Override
	public boolean isExpired() {
		if(expiresIn == null) return false;
		return expirationDate.compareTo(new Date()) <= 0;
	}

	public void refresh(OAuth2AccessToken oauth2AccessToken) {
		setAccessToken(oauth2AccessToken.getAccessToken());
		setExpiresIn(oauth2AccessToken.getExpiresIn());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> U getUser(Class<U> cl) {
		if(user != null && user.getClass().isAssignableFrom(cl)) {
			return (U)user;
		}
		return null;
	}

	@Override
	public void setUser(Object user) {
		this.user = user;
	}
}
