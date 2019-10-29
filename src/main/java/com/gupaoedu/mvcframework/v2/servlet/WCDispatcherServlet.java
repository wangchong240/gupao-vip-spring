package com.gupaoedu.mvcframework.v2.servlet;

import com.gupaoedu.mvcframework.annotation.WCController;
import com.gupaoedu.mvcframework.annotation.WCService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * @author Wang Chong at 2019-10-28 21:12
 * @since V1.0.0
 */
public class WCDispatcherServlet extends HttpServlet {

    //存储主配置的内容
    private Properties contextConfig = new Properties();

    //存储所有扫描类的名字
    private List<String> classNames = new ArrayList<>();

    //暂时用HashMap当ioc容器
    private Map<String, Object> ioc = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2.扫描类
        doScanner(contextConfig.getProperty("scanPackage"));
        //3.初始化扫描类，并放入容器中
        doInstance();
        //4.完成依赖注入
        doAutowired();
        //5.初始化HandlerMapping
        initHandlerMapping();

        System.out.println("初始化完成！！！");
    }

    /**
     * 初始化HandlerMapping
     */
    private void initHandlerMapping() {
    }

    /**
     * 依赖注入
     */
    private void doAutowired() {
    }

    /**
     * 初始化类，并放入容器中
     */
    private void doInstance() {
        if(classNames.isEmpty()) {return;}

        try{
            for(String name : classNames) {
                Class clazz = Class.forName(name);
                //controller初始化
                boolean isInitController = clazz.isAnnotationPresent(WCController.class);
                if(isInitController) {
                   String beanName = toLowerFirstCase(clazz.getSimpleName());
                   Object instance = clazz.newInstance();
                   ioc.put(beanName, instance);
                   continue;
                }
                //service初始化，需要考虑：1.自定义beanName;2.controller以类型注入
                boolean isInitService = clazz.isAnnotationPresent(WCService.class);
                if(isInitService) {
                    //自定义beanName
                    WCService wcService = (WCService) clazz.getAnnotation(WCService.class);
                    String beanName = wcService.value();
                    if("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //获取类的接口，将接口名字作为key，便于按类型注入
                    for(Class i : clazz.getInterfaces()) {
                        if(ioc.containsKey(i.getName())) {
                            throw new Exception("The “" + i.getName() + "“ is exist!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 首字母转小写：规定只能传入驼峰命名
     * @param simpleName 类的名字
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //判断首字母是否大写
        boolean isCap = (chars[0] >= 65 && chars[0] <= 90);
        if(isCap) {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }

    /**
     * 扫描类
     * @param scanPackage 扫描类的包路径
     */
    private void doScanner(String scanPackage) {
        //根据扫描路径，获取类文件路径
        URL url = this.getClass().getClassLoader()
                .getResource("/" + scanPackage.replaceAll("\\.", "/"));
        //获取所有类文件
        if(null == url) {
            return ;
        }
        File classPath = new File(url.getFile());
        //遍历获取类的名字
        for (File file1 : classPath.listFiles()) {
            if (file1.isDirectory()) {
                doScanner(scanPackage + "." + file1.getName());
            } else {
                if (!file1.getName().equals(".class")) {
                    continue;
                }
                String className = file1.getName().replaceAll(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置文件
     * @param contextConfigLocation web.xml配置的配置文件名字
     */
    private void doLoadConfig(String contextConfigLocation) {
        //类路径下classpath：找到主配置文件，读入properties中
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
