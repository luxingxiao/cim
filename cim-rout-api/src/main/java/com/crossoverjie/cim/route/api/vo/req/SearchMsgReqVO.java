package com.crossoverjie.cim.route.api.vo.req;

import com.crossoverjie.cim.common.req.BaseRequest;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

public class SearchMsgReqVO extends BaseRequest {

    @NotNull(message = "userId 不能为空")
    @ApiModelProperty(required = true, value = "消息发送者的 userId", example = "1545574049323")
    private Long sendUserId ;


    @NotNull(message = "userId 不能为空")
    @ApiModelProperty(required = true, value = "消息接收者的 userId", example = "1545574049323")
    private Long receiveUserId ;

    @NotNull(message = "page 不能为空")
    @ApiModelProperty(required = true, value = "页码", example = "1")
    private Integer page ;

    @NotNull(message = "size 不能为空")
    @ApiModelProperty(required = true, value = "每页显示条数", example = "15")
    private Integer size ;

    @NotNull(message = "searchDate 不能为空")
    @ApiModelProperty(required = true, value = "查询searchDate当天的消息", example = "20210223")
    private String searchDate;

    public SearchMsgReqVO() {
    }

    public Long getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(Long sendUserId) {
        this.sendUserId = sendUserId;
    }

    public Long getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(Long receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(String searchDate) {
        this.searchDate = searchDate;
    }

    @Override
    public String toString() {
        return "SearchMsgReqVO{" +
                "sendUserId=" + sendUserId +
                ", receiveUserId=" + receiveUserId +
                ", page=" + page +
                ", size=" + size +
                ", searchDate='" + searchDate + '\'' +
                '}';
    }
}
