package com.basepro.app.controller;

import com.basepro.app.dto.InquirySendReq;
import com.basepro.app.dto.WantReq;
import com.basepro.business.dto.OrderQuery;
import com.basepro.business.entity.BuBookOrder;
import com.basepro.business.entity.BuInquiry;
import com.basepro.business.entity.BuInquiryMsg;
import com.basepro.business.service.BuInquiryService;
import com.basepro.business.service.BuOrderService;
import com.basepro.common.PageQuery;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "用户端预约与留言")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AppTradeController {

    private final BuOrderService orderService;
    private final BuInquiryService inquiryService;

    @PostMapping("/order/want")
    public R<Long> want(@Valid @RequestBody WantReq req) {
        return R.ok(orderService.want(req));
    }

    @GetMapping("/order/page")
    public R<PageResult<BuBookOrder>> orders(@Valid OrderQuery query) {
        return R.ok(orderService.myPage(query));
    }

    @GetMapping("/order/pending-count")
    public R<Long> pendingCount() {
        return R.ok(orderService.pendingWantCount());
    }

    @GetMapping("/order/get")
    public R<BuBookOrder> order(@RequestParam Long id) {
        return R.ok(orderService.get(id, false));
    }

    @PutMapping("/order/agree")
    public R<Void> agree(@RequestParam Long id) {
        orderService.agree(id);
        return R.ok();
    }

    @PutMapping("/order/complete")
    public R<Void> complete(@RequestParam Long id) {
        orderService.confirmMeet(id);
        return R.ok();
    }

    @PutMapping("/order/cancel")
    public R<Void> cancel(@RequestParam Long id, @RequestParam(required = false) String reason) {
        orderService.cancel(id, reason);
        return R.ok();
    }

    @GetMapping("/inquiry/unread-count")
    public R<Long> unreadCount() {
        return R.ok(inquiryService.unreadCount());
    }

    @GetMapping("/inquiry/page")
    public R<PageResult<BuInquiry>> inquiries(@Valid PageQuery query) {
        return R.ok(inquiryService.myPage(query));
    }

    @GetMapping("/inquiry/messages")
    public R<List<BuInquiryMsg>> messages(@RequestParam Long inquiryId) {
        return R.ok(inquiryService.messages(inquiryId, false));
    }

    @PostMapping("/inquiry/send")
    public R<Long> send(@Valid @RequestBody InquirySendReq req) {
        return R.ok(inquiryService.send(req));
    }

    /** 打开与卖家的会话（无消息也可进入聊天页） */
    @PostMapping("/inquiry/open")
    public R<Long> open(@RequestParam Long bookId) {
        return R.ok(inquiryService.openByBook(bookId));
    }

    @PostMapping("/inquiry/reply")
    public R<Void> reply(@RequestParam Long inquiryId, @RequestBody Map<String, String> body) {
        inquiryService.reply(inquiryId, body == null ? null : body.get("content"));
        return R.ok();
    }

    @PutMapping("/inquiry/recall")
    public R<Void> recall(@RequestParam Long msgId) {
        inquiryService.recall(msgId);
        return R.ok();
    }

}
