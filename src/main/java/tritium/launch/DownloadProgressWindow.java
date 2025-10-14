package tritium.launch;

import javax.swing.*;
import java.awt.*;

public class DownloadProgressWindow extends JFrame {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public DownloadProgressWindow() {
        // 设置窗口标题
        setTitle("下载依赖中...");
        
        // 设置窗口大小
        setSize(400, 150);
        
        // 设置窗口关闭操作
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 设置窗口居中显示
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true); // 显示百分比文字
        
        // 创建状态标签
        statusLabel = new JLabel("准备下载...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 添加组件到面板
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        // 添加面板到窗口
        add(panel);

        this.toFront();
    }
    
    /**
     * 设置下载进度
     * @param progress 进度值 (0-100)
     */
    public void setDownloadProgress(int progress) {
        // 确保进度值在0-100之间
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        
        // 使用SwingUtilities确保在EDT线程中更新UI
        int finalProgress = progress;
        SwingUtilities.invokeLater(() -> progressBar.setValue(finalProgress));
    }
    
    /**
     * 设置状态文本
     * @param text 状态文本
     */
    public void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

}