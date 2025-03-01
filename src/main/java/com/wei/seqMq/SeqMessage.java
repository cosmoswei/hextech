package com.wei.seqMq;

import lombok.Data;

@Data
public class SeqMessage {
    private String seqMessageId;
    private Object seqMessageData;
}
