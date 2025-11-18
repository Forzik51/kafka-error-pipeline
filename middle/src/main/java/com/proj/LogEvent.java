package com.proj;

import java.util.Map;

public record LogEvent (
     long ts,
     String service,
     String level,
     String message,
     String traceId,
     Map<String, Object> meta

){}
