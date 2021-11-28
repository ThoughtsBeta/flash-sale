package com.actionworks.flashsale.controller.resource;

import com.actionworks.flashsale.app.service.activity.FlashActivityAppService;
import com.actionworks.flashsale.app.model.command.FlashActivityPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashActivityDTO;
import com.actionworks.flashsale.app.model.query.FlashActivitiesQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.controller.model.builder.FlashActivityBuilder;
import com.actionworks.flashsale.controller.model.builder.ResponseBuilder;
import com.actionworks.flashsale.controller.model.request.FlashActivityPublishRequest;
import com.actionworks.flashsale.controller.model.response.FlashActivityResponse;
import com.actionworks.flashsale.domain.model.enums.FlashActivityStatus;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.actionworks.flashsale.controller.model.builder.FlashActivityBuilder.toFlashActivitiesResponse;
import static com.actionworks.flashsale.controller.model.builder.FlashActivityBuilder.toFlashActivityResponse;

@RestController
public class FlashActivityController {

    @Resource
    private FlashActivityAppService flashActivityAppService;

    @PostMapping(value = "/flash-activities")
    public Response publishFlashActivity(@RequestAttribute Long userId, @RequestBody FlashActivityPublishRequest flashActivityPublishRequest) {
        FlashActivityPublishCommand activityPublishCommand = FlashActivityBuilder.toCommand(flashActivityPublishRequest);
        AppResult appResult = flashActivityAppService.publishFlashActivity(userId, activityPublishCommand);
        return ResponseBuilder.with(appResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}")
    public Response modifyFlashActivity(@RequestAttribute Long userId, @PathVariable Long activityId, @RequestBody FlashActivityPublishRequest flashActivityPublishRequest) {
        FlashActivityPublishCommand activityPublishCommand = FlashActivityBuilder.toCommand(flashActivityPublishRequest);
        AppResult appResult = flashActivityAppService.modifyFlashActivity(userId, activityId, activityPublishCommand);
        return ResponseBuilder.with(appResult);
    }

    @GetMapping(value = "/flash-activities")
    @SentinelResource("GetActivitiesResource")
    public MultiResponse<FlashActivityResponse> getFlashActivities(@RequestAttribute Long userId,
                                                                   @RequestParam Integer pageSize,
                                                                   @RequestParam Integer pageNumber,
                                                                   @RequestParam(required = false) String keyword) {
        FlashActivitiesQuery flashActivitiesQuery = new FlashActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);

        AppMultiResult<FlashActivityDTO> flashActivitiesResult = flashActivityAppService.getFlashActivities(userId, flashActivitiesQuery);
        return ResponseBuilder.withMulti(flashActivitiesResult);
    }

    @GetMapping(value = "/flash-activities/online")
    @SentinelResource("GetOnlineActivitiesResource")
    public MultiResponse<FlashActivityResponse> getOnlineFlashActivities(@RequestAttribute Long userId,
                                                                         @RequestParam Integer pageSize,
                                                                         @RequestParam Integer pageNumber,
                                                                         @RequestParam(required = false) String keyword) {
        FlashActivitiesQuery flashActivitiesQuery = new FlashActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(FlashActivityStatus.ONLINE.getCode());

        AppMultiResult<FlashActivityDTO> flashActivitiesResult = flashActivityAppService.getFlashActivities(userId, flashActivitiesQuery);
        if (!flashActivitiesResult.isSuccess() || flashActivitiesResult.getData() == null) {
            return ResponseBuilder.withMulti(flashActivitiesResult);
        }
        return MultiResponse.of(toFlashActivitiesResponse(flashActivitiesResult.getData()), flashActivitiesResult.getTotal());
    }

    @GetMapping(value = "/flash-activities/{activityId}")
    @SentinelResource("GetActivityResource")
    public SingleResponse<FlashActivityResponse> getFlashActivity(@RequestAttribute Long userId,
                                                                  @PathVariable Long activityId,
                                                                  @RequestParam(required = false) Long version) {
        AppSimpleResult<FlashActivityDTO> flashActivityResult = flashActivityAppService.getFlashActivity(userId, activityId, version);
        if (!flashActivityResult.isSuccess() || flashActivityResult.getData() == null) {
            return ResponseBuilder.withSingle(flashActivityResult);
        }
        FlashActivityDTO flashActivityDTO = flashActivityResult.getData();
        return SingleResponse.of(toFlashActivityResponse(flashActivityDTO));
    }

    @PutMapping(value = "/flash-activities/{activityId}/online")
    public Response onlineFlashActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult appResult = flashActivityAppService.onlineFlashActivity(userId, activityId);
        return ResponseBuilder.with(appResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}/offline")
    public Response offlineFlashActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult appResult = flashActivityAppService.offlineFlashActivity(userId, activityId);
        return ResponseBuilder.with(appResult);
    }
}
