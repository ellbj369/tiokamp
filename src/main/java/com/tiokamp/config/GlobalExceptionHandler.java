package com.tiokamp.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Thrown while parsing the multipart request, before any controller runs —
     * without this handler the user is silently bounced to /login with no message.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(HttpServletRequest request) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("error", "Bilden är för stor (max 15 MB). Välj en mindre bild och försök igen.");
        return "redirect:/register";
    }
}
