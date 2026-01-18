package com.chip.board.baselinesync.application.service;

import com.chip.board.baselinesync.application.component.writer.BaselineJobWriter;
import com.chip.board.baselinesync.application.port.BaselineEnqueuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BaselineEnqueueService implements BaselineEnqueuePort {

    private final BaselineJobWriter jobWriter;

    @Override
    public void enqueueBaseline(long userId) {
        jobWriter.scheduleNow(userId);
    }
}