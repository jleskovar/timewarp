/*
 * Copyright (c) 2015, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are license under the terms of the
 * MIT license.
 */
package co.paralleluniverse.vtime;

import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public final class DispatcherServlet_ {

    private DispatcherServlet_() {
    }

    public static void DispatcherServlet_doDispatch(DispatcherServlet dispatcherServlet, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (TimeWarpController.getInstance().canHandle(request)) {
            TimeWarpController.getInstance().handleServletRequest(request, response);
            return;
        }

        Method doDispatch = dispatcherServlet.getClass().getDeclaredMethod("doDispatch", request.getClass().getInterfaces()[0], response.getClass().getInterfaces()[0]);
        doDispatch.setAccessible(true);
        doDispatch.invoke(dispatcherServlet, request, response);
    }

    public static void MortbayJetty_Handler_handle(org.mortbay.jetty.Handler handler,
                                                   String target,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   int dispatch) throws Exception {

        if (TimeWarpController.getInstance().canHandle(request)) {
            TimeWarpController.getInstance().handleServletRequest(request, response);
            return;
        }

        Method handle = handler.getClass().getMethod("handle",
                String.class,
                request.getClass().getInterfaces()[0],
                response.getClass().getInterfaces()[0],
                int.class);

        handle.setAccessible(true);
        handle.invoke(handler, target, request, response, dispatch);
    }

}
