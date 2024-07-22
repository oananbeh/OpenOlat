/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * The security check is done by the curriculums web service.
 * 
 * Initial date: 16 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Curriculum")
@Component
@Path("curriculum/types")
public class CurriculumElementTypesWebService {
	
	@Autowired
	private CurriculumService curriculumService;
	
	/**
	 * Return the curriculum element types used in the whole OpenOLAT instance.
	 * 
	 * @param httpRequest  The HTTP request
	 * @return An array of curriculum element types
	 */
	@GET
	@Operation(summary = "Return the curriculum element types",
		description = "Return the curriculum element types used in the whole OpenOLAT instance")
	@ApiResponse(responseCode = "200", description = "An array of curriculum element types",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CurriculumElementTypeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CurriculumElementTypeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getElementTypes(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		List<CurriculumElementType> elementTypes = curriculumService.getCurriculumElementTypes();
		List<CurriculumElementTypeVO> voes = new ArrayList<>(elementTypes.size());
		for(CurriculumElementType elementType:elementTypes) {
			voes.add(CurriculumElementTypeVO.valueOf(elementType));
		}
		return Response.ok(voes.toArray(new CurriculumElementTypeVO[voes.size()])).build();
	}
	

	/**
	 * Creates and persists a new curriculum element type entity.
	 * 
	 * @param curriculumelementType The curriculum element type to persist
	 * @return The new persisted <code>curriculum element type</code>
	 */
	@PUT
	@Operation(summary = "Create and persists a new curriculum element type entity",
		description = "Creates and persists a new curriculum element type entity")
	@ApiResponse(responseCode = "200", description = "The persisted curriculum element type",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumElementTypeVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumElementTypeVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient.")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json.")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCurriculumElementType(CurriculumElementTypeVO curriculumelementType, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		CurriculumElementType savedElementType = saveCurriculumElementType(curriculumelementType);
		return Response.ok(CurriculumElementTypeVO.valueOf(savedElementType)).build();
	}
	
	/**
	 * Updates a new curriculum element type entity.
	 * 
	 * @param curriculumElementType The curriciulum element type to merge
	 * @return The merged <code>curriculum element type</code>
	 */
	@POST
	@Operation(summary = "Update a new curriculum element type entity",
		description = "Updates a new curriculum element type entity")
	@ApiResponse(responseCode = "200", description = "The merged curriculum element type",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumElementTypeVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumElementTypeVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient.")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json.")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculumElementType(CurriculumElementTypeVO curriculumElementType, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		CurriculumElementType savedElementType = saveCurriculumElementType(curriculumElementType);
		return Response.ok(CurriculumElementTypeVO.valueOf(savedElementType)).build();
	}
	
	/**
	 * Updates a new curriculum element type entity. The primary key is taken from
	 * the URL. The curriculum element type object can be "primary key free".
	 * 
	 * @param curriculumElementTypeKey The curriculum element type primary key
	 * @param curriculumElementType The curriculum element type to merge
	 * @return The merged <code>curriculum element type</code>
	 */
	@POST
	@Path("{curriculumElementTypeKey}")
	@Operation(summary = "Update a new curriculum element type entity",
		description = "Updates a new curriculum element type entity. The primary key is taken from\n" + 
			" the URL. The curriculum element type object can be \"primary key free\"")
	@ApiResponse(responseCode = "200", description = "The merged type curriculum element",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumElementTypeVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumElementTypeVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient.")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json.")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculumElementType(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey,
			CurriculumElementTypeVO curriculumElementType, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		if(curriculumElementType.getKey() == null) {
			curriculumElementType.setKey(curriculumElementTypeKey);
		} else if(!curriculumElementTypeKey.equals(curriculumElementType.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		CurriculumElementType savedElementType = saveCurriculumElementType(curriculumElementType);
		return Response.ok(CurriculumElementTypeVO.valueOf(savedElementType)).build();
	}
	
	private CurriculumElementType saveCurriculumElementType(CurriculumElementTypeVO elementTypeVo) {
		CurriculumElementType elementType;
		if(elementTypeVo.getKey() == null) {
			elementType = curriculumService.createCurriculumElementType(elementTypeVo.getIdentifier(),
					elementTypeVo.getDisplayName(), elementTypeVo.getDescription(), elementTypeVo.getExternalId());
		} else {
			elementType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(elementTypeVo.getKey()));
			elementType.setDisplayName(elementTypeVo.getDisplayName());
			elementType.setIdentifier(elementTypeVo.getIdentifier());
			elementType.setDescription(elementTypeVo.getDescription());
			elementType.setExternalId(elementTypeVo.getExternalId());
		}
		elementType.setCssClass(elementTypeVo.getCssClass());
		if(StringHelper.containsNonWhitespace(elementTypeVo.getCalendars())) {
			elementType.setCalendars(CurriculumCalendars.valueOf(elementTypeVo.getCalendars()));
		} else {
			elementType.setCalendars(CurriculumCalendars.disabled);
		}
		if(StringHelper.containsNonWhitespace(elementTypeVo.getLectures())) {
			elementType.setLectures(CurriculumLectures.valueOf(elementTypeVo.getLectures()));
		} else {
			elementType.setLectures(CurriculumLectures.disabled);
		}
		
		elementType.setManagedFlags(CurriculumElementTypeManagedFlag.toEnum(elementTypeVo.getManagedFlagsString()));
		return curriculumService.updateCurriculumElementType(elementType);
	}
	
	/**
	 * Get a specific curriculum element type.
	 * 
	 * @param curriculumElementTypeKey The curriculum element type primary key
	 * @return The curriculum element type
	 */
	@GET
	@Path("{curriculumElementTypeKey}")
	@Operation(summary = "Get a specific curriculum element type",
		description = "Get a specific curriculum element type")
	@ApiResponse(responseCode = "200", description = "The merged type curriculum element",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumElementTypeVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumElementTypeVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculumElementTypes(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		CurriculumElementType elementType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		if(elementType == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(CurriculumElementTypeVO.valueOf(elementType)).build();
	}	

	/**
	 * Get the allowed sub-types of a specified curriculum element type.
	 * 
	 * @param curriculumElementTypeKey The curriculum element type primary key
	 * @return An array of curriculum element types
	 */
	@GET
	@Path("{curriculumElementTypeKey}/allowedSubTypes")
	@Operation(summary = "Get the allowed sub-types",
		description = "Get the allowed sub-types of a specified curriculum element type")
	@ApiResponse(responseCode = "200", description = "An array of curriculum element types",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CurriculumElementTypeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CurriculumElementTypeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "405", description = "The curriculum element type was not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getAllowedSubTypes(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		CurriculumElementType type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		if(type == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Set<CurriculumElementTypeToType> typeToTypes = type.getAllowedSubTypes();
		List<CurriculumElementTypeVO> subTypeVOes = new ArrayList<>(typeToTypes.size());
		for(CurriculumElementTypeToType typeToType:typeToTypes) {
			CurriculumElementType subType = typeToType.getAllowedSubType();
			subTypeVOes.add(CurriculumElementTypeVO.valueOf(subType));
		}
		return Response.ok(subTypeVOes.toArray(new CurriculumElementTypeVO[subTypeVOes.size()])).build();
	}
	
	/**
	 * Add a sub-type to a specified curriculum element type.
	 * 
	 * @param curriculumElementTypeKey The type
	 * @param subTypeKey The sub type
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementTypeKey}/allowedSubTypes/{subTypeKey}")
	@Operation(summary = "Add a sub-type to a specified curriculum element type",
		description = "Add a sub-type to a specified curriculum element type")
	@ApiResponse(responseCode = "200", description = "The sub type was added to the allowed sub types")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "405", description = "The curriculum element type was not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response allowSubTaxonomyLevelType(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey,
			@PathParam("subTypeKey") Long subTypeKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		CurriculumElementType type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		CurriculumElementType subType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(subTypeKey));
		if(type == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		curriculumService.allowCurriculumElementSubType(type, subType);
		return Response.ok().build();
	}
	
	/**
	 * Remove a sub-type to a specified curriculum element type.
	 * 
	 * @param curriculumElementTypeKey The type
	 * @param subTypeKey The sub type to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementTypeKey}/allowedSubTypes/{subTypeKey}")
	@Operation(summary = "Remove a sub-type to a specified curriculum element type",
		description = "Remove a sub-type to a specified curriculum element type")
	@ApiResponse(responseCode = "200", description = "The sub type was removed successfully")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "405", description = "The curriculum element type was not found")
	public Response disalloweSubType(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey,
			@PathParam("subTypeKey") Long subTypeKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
		}
		
		CurriculumElementType type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		CurriculumElementType subType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(subTypeKey));
		if(type == null || subType == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		curriculumService.disallowCurriculumElementSubType(type, subType);
		return Response.ok().build();
	}

}
