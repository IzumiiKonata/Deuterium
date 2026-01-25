package tritium.rendering.notification;

import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    @Getter
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void doRender(double posX, double posY) {
        double startY = posY;
        for (Notification notification : notifications) {
            if (notification == null)
                continue;

            notification.draw(posX, startY);

            startY += notification.height + 8;
        }

        notifications.removeIf(Notification::shouldDelete);
    }

}
