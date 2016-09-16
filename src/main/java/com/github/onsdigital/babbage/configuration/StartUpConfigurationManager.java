package com.github.onsdigital.babbage.configuration;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.davidcarboni.restolino.reload.ClassFinder;
import org.reflections.Reflections;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dave on 9/1/16.
 */
public class StartUpConfigurationManager implements Startup {

    private static String MESSAGE = MessageFormat.format("[{0}] \n", StartUpConfigurationManager.class.getSimpleName());

    @Override
    public void init() {
        Reflections reflections = ClassFinder.newReflections();
        Set<Class<? extends StartUpConfiguration>> configurationTasks = reflections.getSubTypesOf(StartUpConfiguration.class);
        Iterator<Class<? extends StartUpConfiguration>> taskIterator = configurationTasks.iterator();
        System.out.println("\n" + MESSAGE);

        while (taskIterator.hasNext()) {
            Class< ? extends StartUpConfiguration> taskClazz = taskIterator.next();
            try {
                Class<?> clazz = Class.forName(taskClazz.getName());
                StartUpConfiguration task = (StartUpConfiguration)clazz.newInstance();
                task.init();
                if (taskIterator.hasNext()) {
                    System.out.println("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("\n" + MESSAGE);
    }
}
