package org.example.mvc;

import org.example.mvc.controller.RequestMethod;
import org.example.mvc.view.JspViewResolver;
import org.example.mvc.view.ModelAndView;
import org.example.mvc.view.View;
import org.example.mvc.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet("/")
public class DispatcherServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private List<HandlerMapping> handlerMappings;

    private List<HandlerAdapter> handlerAdapters;

    private List<ViewResolver> viewResolvers;

    @Override
    public void init() throws ServletException {
        RequestMappingHanlderMapping rmhm = new RequestMappingHanlderMapping();
        rmhm.init();

        AnnotationHandlerMapping ahm = new AnnotationHandlerMapping("org.example");
        ahm.initialize();

        handlerMappings = List.of(rmhm, ahm);
        handlerAdapters = List.of(new SimpleControllerHandlerAdapter(), new AnnotationHandlerAdapter());
        viewResolvers = Collections.singletonList(new JspViewResolver());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("[DispatcherServlet] service started.");
        String requestURI = req.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(req.getMethod());

        try {
            Object handler = handlerMappings.stream()
                    .filter(hm -> hm.findHanlder(new HandlerKey(requestMethod, requestURI)) != null )
                    .map(hm -> hm.findHanlder(new HandlerKey(requestMethod,requestURI)))
                    .findFirst()
                    .orElseThrow(()->new ServletException("no handler for [" + requestMethod + ", " +requestURI + "]"));

            // "redirect:/users" vs forward
            // handleradapter 생성으로 필요없어짐
            // String viewName = handler.handlerRequest(req,resp);

            HandlerAdapter handlerAdapter = handlerAdapters.stream()
                    .filter(ha -> ha.supports(handler))
                    .findFirst()
                    .orElseThrow(()->new ServletException("No adapter for request: [" + handler + "]"));

            ModelAndView modelAndView = handlerAdapter.handle(req,resp,handler);

            for (ViewResolver viewResolver : viewResolvers) {
                View view = viewResolver.resolveView(modelAndView.getViewName());
                view.render(modelAndView.getModel(), req,resp);
            }

        } catch (Exception e) {
           log.error("exception occurred:[{}]", e.getMessage(),e);
           throw new ServletException(e);
        }
    }
}
