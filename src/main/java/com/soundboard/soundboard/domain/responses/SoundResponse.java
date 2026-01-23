//package com.soundboard.soundboard.domain.responses;
//
//import java.time.Instant;
//
//public record SoundResponse(
//        String status,
//        SoundData data,
//        Instant timeStamp
//) {
//  public static SoundResponse success(SoundData data) {
//    return new SoundResponse("success", data, Instant.now());
//  }
//}
