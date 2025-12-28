package net.minecraft.client.main;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

public class Main {
    
    public static void main(String[] args) {
        String javaVersion = System.getProperty("java.version");
        int majorVersion = getMajorVersion(javaVersion);
        
        System.out.println("当前 Java 版本: " + javaVersion);
        System.out.println("主版本号: " + majorVersion);
        
        if (majorVersion < 21) {
            showJavaVersionWarning();
        } else {
            try {
                Class<?> mainClass = Class.forName("tritium.launch.DependencyDownloader");
                Object o = mainClass.getConstructor().newInstance();
                Method mainMethod = mainClass.getMethod("run", String[].class);
                mainMethod.invoke(o, (Object) args);
            } catch (Exception e) {
                System.err.println("无法启动主类: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    private static int getMajorVersion(String version) {
        try {
            if (version.startsWith("1.")) {
                return Integer.parseInt(version.substring(2, 3));
            } else {
                int dotIndex = version.indexOf('.');
                return Integer.parseInt(dotIndex > 0 ? version.substring(0, dotIndex) : version);
            }
        } catch (NumberFormatException e) {
            return 21;
        }
    }
    
    private static void showJavaVersionWarning() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        
        JFrame frame = new JFrame("Java 版本检查");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        iconPanel.add(iconLabel);
        
        JPanel messagePanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JLabel titleLabel = new JLabel("Java 版本过低", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.RED);
        
        JLabel messageLabel2 = new JLabel("Tritium需要Java 21或更高版本才能正常运行", SwingConstants.CENTER);
        messageLabel2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JLabel messageLabel3 = new JLabel("请升级到Java 21后重新启动", SwingConstants.CENTER);
        messageLabel3.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        messagePanel.add(titleLabel);
        messagePanel.add(messageLabel2);
        messagePanel.add(messageLabel3);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton exitButton = new JButton("退出");
        exitButton.setPreferredSize(new Dimension(100, 30));
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(exitButton);
        
        mainPanel.add(iconPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        
        frame.setVisible(true);
    }

}