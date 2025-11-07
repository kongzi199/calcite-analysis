package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameSelectionFrame extends JFrame {
    public GameSelectionFrame() {
        setTitle("游戏选择");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2, 1, 10, 10));

        // 创建五子棋按钮
        JButton gobangButton = new JButton("五子棋");
        gobangButton.setFont(new Font("宋体", Font.BOLD, 24));
        gobangButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 打开五子棋游戏窗口
                new GobangGameFrame();
                dispose(); // 关闭当前选择窗口
            }
        });

        // 创建翻转棋按钮
        JButton reversiButton = new JButton("翻转棋");
        reversiButton.setFont(new Font("宋体", Font.BOLD, 24));
        reversiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 这里可以添加翻转棋游戏的实现
                JOptionPane.showMessageDialog(GameSelectionFrame.this, "翻转棋功能开发中...");
            }
        });

        // 添加按钮到窗口
        add(gobangButton);
        add(reversiButton);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameSelectionFrame();
            }
        });
    }
}