package ru.ruscreat.shareSsau.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // Добавляем информацию для отладки
        model.addAttribute("errorStatus", status);
        model.addAttribute("errorMessage", message);
        model.addAttribute("errorPath", path);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "404";
            }
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorDetails", "Доступ запрещен. Проверьте, что у вас есть необходимые права.");
            }
        }
        
        return "error";
    }
}
