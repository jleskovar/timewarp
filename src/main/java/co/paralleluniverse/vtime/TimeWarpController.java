package co.paralleluniverse.vtime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by james on 25/04/16.
 */
public class TimeWarpController {
    private static TimeWarpController instance;

    public static TimeWarpController getInstance() {
        return instance == null ? new TimeWarpController() : instance;
    }

    public void handleServletRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String method = getParameter(request, "method");
        method = method == null ? getMethod(request) : method;
        switch (method) {
            case "GET":
                printCurrentAndRealTime(response);
                break;

            case "POST":
                String time = getParameter(request, "time");
                if (time != null && !"".equals(time)) {
                    TimeControllerAccess.getTimeController().jumpToTime(time);
                    printCurrentAndRealTime(response);
                } else {
                    setStatus(response, 400);
                    printLines(response, "{ \"error\" : \"Missing 'time' parameter\" }");
                }
                break;
        }
    }

    private void printCurrentAndRealTime(HttpServletResponse response) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try (PrintWriter printWriter = getPrintWriter(response)) {
            printWriter.println(
                    "{\"currentTime\": \"" + TimeControllerAccess.getTimeController().getCurrentTime() + "\"," +
                    " \"realTime\": \"" + TimeControllerAccess.getTimeController().getRealTime() + "\" }"
            );
        }
    }

    private void printLines(HttpServletResponse response, String... lines) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try (PrintWriter printWriter = getPrintWriter(response)) {
            for (String line : lines) {
                printWriter.println(line);
            }
        }
    }

    public boolean canHandle(HttpServletRequest request) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return getPathInfo(request).startsWith("/timecontrol");
    }

    private String getPathInfo(HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invokeOn(request, "getPathInfo");
    }

    private String getQueryString(HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invokeOn(request, "getQueryString");
    }

    private String getParameter(HttpServletRequest request, String parameter) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invokeOn(request, "getParameter", parameter);
    }

    private PrintWriter getPrintWriter(HttpServletResponse response) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return invokeOn(response, "getWriter");
    }

    private String getMethod(HttpServletRequest request) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return invokeOn(request, "getMethod");
    }

    private void setStatus(HttpServletResponse response, int status) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        response.getClass().getMethod("setStatus", int.class).invoke(response, status);
    }

    private static <T> T invokeOn(Object object, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (Method m : object.getClass().getMethods()) {
            if (methodName.equals(m.getName()))
                return (T) m.invoke(object, args);
        }
        return null;
    }
}
