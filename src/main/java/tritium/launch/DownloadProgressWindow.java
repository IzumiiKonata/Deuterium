package tritium.launch;

import javax.swing.*;
import java.awt.*;

public class DownloadProgressWindow extends JFrame {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public DownloadProgressWindow() {
        setTitle("下载依赖中...");
        
        setSize(400, 150);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        
        statusLabel = new JLabel("准备下载...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        add(panel);

        this.toFront();
    }
    
    /**
     * 设置下载进度
     * @param progress 进度值 (0-100)
     */
    public void setDownloadProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        
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