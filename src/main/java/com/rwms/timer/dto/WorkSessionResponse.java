package com.rwms.timer.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSessionResponse {

    private String sessionState;
    private long workedSeconds;
    private long breakSeconds;
    private boolean breakTaken;
    private boolean breakWarning;
    private boolean breakEndingWarning;
    private boolean triggerSubmitPage;
}
