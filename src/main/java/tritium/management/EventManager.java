package tritium.management;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import tritium.event.eventapi.*;
import tritium.interfaces.SharedConstants;
import tritium.event.eventapi.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * the event bus.
 *
 * @author IzumiiKonata
 * @since 3/25/2023 11:04 PM
 */
public class EventManager extends AbstractManager implements SharedConstants {
    // 方法Map
    private static final Map<Type, List<Target>> registrationMap = new HashMap<>();

    public EventManager() {
        super("EventManager");
    }

    static MethodHandles.Lookup lookup = MethodHandles.lookup();

    /**
     * 注册该对象中所有被 @Handler 修饰的方法
     *
     * @param obj 类实例
     */
    @SneakyThrows
    public static void register(Object obj) {
        for (Method method : obj.getClass().getDeclaredMethods()) {

            if (method.isAnnotationPresent(Handler.class)) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                int pr;
                if (method.isAnnotationPresent(Priority.class)) {
                    Priority priority = method.getAnnotation(Priority.class);
                    pr = priority.priority().getLevel();
                } else {
                    // 如果没有 @Priority 注解的话默认就是 Normal 优先级
                    pr = EnumPriority.NORMAL.getLevel();
                }

//                method.setAccessible(accessible);
                // 获取方法的 Event 类型
                Class<?> eventClass = method.getParameterTypes()[0];

                Target target = new Target(lookup.unreflect(method), eventClass, obj, pr);

                if (registrationMap.containsKey(eventClass)) {
                    if (!registrationMap.get(eventClass).contains(target)) {
                        registrationMap.get(eventClass).add(target);
                    }
                } else {
                    registrationMap.put(eventClass, new CopyOnWriteArrayList<>() {
                        {
                            add(target);
                        }
                    });
                }

                registrationMap.get(eventClass).sort(Comparator.comparingInt(o -> ((Target) o).priority).reversed());
            }

        }

    }


    /**
     * 取消订阅指定类实例中的带有 @Handler 注解的方法.
     *
     * @param source 类实例
     */
    public static void unregister(Object source) {
        for (List<Target> dataList : registrationMap.values()) {
            dataList.removeIf(data -> data.getSource().equals(source));
        }

        cleanMap(true);
    }

    /**
     * 清理 Map 中的空 Entry
     */
    public static void cleanMap(boolean onlyEmptyEntries) {
        Iterator<Map.Entry<Type, List<Target>>> mapIterator = registrationMap.entrySet().iterator();

        while (mapIterator.hasNext()) {
            if (!onlyEmptyEntries || mapIterator.next().getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    /**
     * 是否有方法可以接受这个 event
     *
     * @param eventClass event类型
     * @return 是否有方法可以接受这个 event
     */
    @SneakyThrows
    public boolean canReceive(Class<? extends Event> eventClass) {
        List<Target> methodList = registrationMap.get(eventClass);

        if (methodList != null) {
            for (Target target : methodList) {
                if (target.getType() == eventClass) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 调用所有注册的 event
     *
     * @param event event 类型
     */
    @SneakyThrows
    public static <T extends Event> T call(T event) {
        List<Target> methodList = registrationMap.get(event.getClass());

        if (methodList != null) {
            for (Target target : methodList) {

                if (target.getType() == event.getClass()) {
                    event.setResponded(true);

                    try {
                        target.getTargetMethod().invoke(target.getSource(), event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (event instanceof EventCancellable && ((EventCancellable) event).isCancelled()) {
                        break;
                    }
                }
            }

        }

        return event;
    }

    @Getter
    @AllArgsConstructor
    private static class Target {
        private MethodHandle targetMethod;
        private Type type;
        private Object source;
        private int priority;
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }

}
