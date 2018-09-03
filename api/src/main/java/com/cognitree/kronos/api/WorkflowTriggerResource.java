/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognitree.kronos.api;

import com.cognitree.kronos.model.WorkflowId;
import com.cognitree.kronos.model.WorkflowTrigger;
import com.cognitree.kronos.model.WorkflowTriggerId;
import com.cognitree.kronos.scheduler.ServiceException;
import com.cognitree.kronos.scheduler.ValidationException;
import com.cognitree.kronos.scheduler.WorkflowService;
import com.cognitree.kronos.scheduler.WorkflowTriggerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

@Path("workflows")
@Api(value = "workflow triggers", description = "manage workflow triggers")
public class WorkflowTriggerResource {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowTriggerResource.class);

    @GET
    @Path("{workflow}/triggers")
    @ApiOperation(value = "Get all workflow triggers", response = WorkflowTrigger.class, responseContainer = "List")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllWorkflowTriggers(@PathParam("workflow") String workflowName,
                                           @HeaderParam("namespace") String namespace) throws ServiceException, ValidationException {
        logger.info("Received request to get all workflow triggers for workflow {} under namespace {}",
                workflowName, namespace);
        final List<WorkflowTrigger> triggers = WorkflowTriggerService.getService().get(workflowName, namespace);
        return Response.status(OK).entity(triggers).build();
    }

    @GET
    @Path("{workflow}/triggers/{name}")
    @ApiOperation(value = "Get workflow trigger with triggerName", response = WorkflowTrigger.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Workflow trigger not found")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkflowTrigger(@ApiParam(value = "workflow triggerName", required = true)
                                       @PathParam("workflow") String workflowName,
                                       @ApiParam(value = "workflow trigger triggerName", required = true)
                                       @PathParam("name") String triggerName,
                                       @HeaderParam("namespace") String namespace) throws ServiceException, ValidationException {
        logger.info("Received request to get workflow trigger with name {} for workflow {} under namespace {}",
                triggerName, workflowName, namespace);
        WorkflowTriggerId triggerId = WorkflowTriggerId.build(triggerName, workflowName, namespace);
        final WorkflowTrigger workflowTrigger = WorkflowTriggerService.getService().get(triggerId);
        if (workflowTrigger == null) {
            logger.error("No workflow trigger exists with name {} under namespace {}", triggerName, namespace);
            return Response.status(NOT_FOUND).build();
        }
        return Response.status(OK).entity(workflowTrigger).build();
    }

    @POST
    @Path("{workflow}/triggers")
    @ApiOperation(value = "Create workflow trigger", response = WorkflowTrigger.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Workflow trigger not found")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWorkflowTrigger(@ApiParam(value = "workflow name", required = true)
                                          @PathParam("workflow") String workflowName,
                                          @HeaderParam("namespace") String namespace,
                                          WorkflowTrigger workflowTrigger) throws ServiceException, ValidationException, SchedulerException {
        // override workflow name and namespace
        workflowTrigger.setWorkflow(workflowName);
        workflowTrigger.setNamespace(namespace);
        logger.info("Received request to create workflow trigger {} for workflow {} under namespace {}",
                workflowTrigger, workflowName, namespace);
        WorkflowId workflowId = WorkflowId.build(workflowName, namespace);
        if (WorkflowService.getService().get(workflowId) == null) {
            logger.error("No workflow exists with name {} under namespace {}", workflowName, namespace);
            return Response.status(BAD_REQUEST).entity("no workflow exists with name " + workflowName).build();
        }

        if (WorkflowTriggerService.getService().get(workflowTrigger) != null) {
            logger.error("Workflow trigger already exists with name {} for workflow {} under namespace {}",
                    workflowTrigger.getName(), workflowName, namespace);
            return Response.status(CONFLICT).build();
        }
        WorkflowTriggerService.getService().add(workflowTrigger);
        return Response.status(CREATED).entity(workflowTrigger).build();
    }

    @PUT
    @Path("{workflow}/triggers/{name}")
    @ApiOperation(value = "Update workflow trigger", response = WorkflowTrigger.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Workflow trigger not found")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWorkflowTrigger(@ApiParam(value = "workflow name", required = true)
                                          @PathParam("workflow") String workflowName,
                                          @ApiParam(value = "workflow trigger name", required = true)
                                          @PathParam("name") String triggerName,
                                          @HeaderParam("namespace") String namespace,
                                          WorkflowTrigger workflowTrigger) throws ServiceException, ValidationException, SchedulerException {
        // override workflow name and namespace
        workflowTrigger.setName(triggerName);
        workflowTrigger.setWorkflow(workflowName);
        workflowTrigger.setNamespace(namespace);
        logger.info("Received request to update workflow trigger {} for workflow {} under namespace {} to {}",
                triggerName, workflowName, namespace, workflowTrigger);
        if (WorkflowTriggerService.getService().get(workflowTrigger) == null) {
            logger.error("No workflow trigger exists with name {} for workflow {} under namespace {}",
                    workflowTrigger.getName(), workflowName, namespace);
            return Response.status(NOT_FOUND).build();
        }
        WorkflowTriggerService.getService().update(workflowTrigger);
        return Response.status(OK).entity(workflowTrigger).build();
    }

    @DELETE
    @Path("{workflow}/triggers/{name}")
    @ApiOperation(value = "Delete workflow trigger with name")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Workflow trigger not found")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWorkflowTrigger(@ApiParam(value = "workflow name", required = true)
                                          @PathParam("workflow") String workflowName,
                                          @ApiParam(value = "workflow trigger name", required = true)
                                          @PathParam("name") String triggerName,
                                          @HeaderParam("namespace") String namespace) throws ServiceException, SchedulerException, ValidationException {
        logger.info("Received request to delete workflow trigger with name {} for workflow {} under namespace {}",
                triggerName, workflowName, namespace);
        WorkflowTriggerId triggerId = WorkflowTriggerId.build(triggerName, workflowName, namespace);
        final WorkflowTrigger workflowTrigger = WorkflowTriggerService.getService().get(triggerId);
        if (workflowTrigger == null) {
            logger.error("No workflow trigger exists with name {} under namespace {}", triggerName, namespace);
            return Response.status(NOT_FOUND).build();
        }
        WorkflowTriggerService.getService().delete(workflowTrigger);
        return Response.status(OK).build();
    }
}