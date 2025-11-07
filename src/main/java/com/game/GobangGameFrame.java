package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GobangGameFrame extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int GRID_SIZE = 40;
    private static final int MARGIN = 50;
    private static final int PIECE_SIZE = 36;
    private static final Color BOARD_COLOR = new Color(245, 222, 179); // 浅黄色
    private static final Color GRID_COLOR = new Color(139, 69, 19); // 深褐色
    private static final Color BLACK_PIECE = Color.BLACK;
    private static final Color WHITE_PIECE = Color.WHITE;
    private static final Color WIN_HIGHLIGHT = Color.RED;

    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE]; // 0: 空, 1: 黑, 2: 白
    private boolean isBlackTurn = true;
    private boolean gameOver = false;
    private boolean professionalMode = false;
    private List<int[]> moveHistory = new ArrayList<>();
    private List<long[]> timeHistory = new ArrayList<>();
    private long startTime;
    private JLabel currentPlayerLabel;
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JPanel chessboardPanel;

    public GobangGameFrame() {
        setTitle("五子棋游戏");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 创建菜单
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("游戏");
        JMenuItem restartItem = new JMenuItem("重新开始");
        restartItem.addActionListener(e -> restartGame());
        JMenuItem undoItem = new JMenuItem("悔棋");
        undoItem.addActionListener(e -> undoMove());
        JCheckBoxMenuItem professionalItem = new JCheckBoxMenuItem("专业模式(禁手)");
        professionalItem.addActionListener(e -> professionalMode = professionalItem.isSelected());
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        gameMenu.add(restartItem);
        gameMenu.add(undoItem);
        gameMenu.add(professionalItem);
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        // 创建棋盘面板
        chessboardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChessboard(g);
                drawPieces(g);
            }
        };
        chessboardPanel.setBackground(BOARD_COLOR);
        chessboardPanel.setPreferredSize(new Dimension(MARGIN * 2 + GRID_SIZE * (BOARD_SIZE - 1),
                MARGIN * 2 + GRID_SIZE * (BOARD_SIZE - 1)));
        chessboardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver) return;
                int x = e.getX();
                int y = e.getY();
                int row = (y - MARGIN + GRID_SIZE / 2) / GRID_SIZE;
                int col = (x - MARGIN + GRID_SIZE / 2) / GRID_SIZE;
                if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                    makeMove(row, col);
                }
            }
        });
        add(chessboardPanel, BorderLayout.CENTER);

        // 创建信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(200, 0));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        currentPlayerLabel = new JLabel("当前执子: 黑方");
        currentPlayerLabel.setFont(new Font("宋体", Font.BOLD, 16));
        currentPlayerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(currentPlayerLabel);

        timeLabel = new JLabel("已用时长: 00:00");
        timeLabel.setFont(new Font("宋体", Font.BOLD, 16));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(timeLabel);

        statusLabel = new JLabel("游戏状态: 进行中");
        statusLabel.setFont(new Font("宋体", Font.BOLD, 16));
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(statusLabel);

        add(infoPanel, BorderLayout.EAST);

        // 开始计时
        startTime = System.currentTimeMillis();
        startTimer();

        setVisible(true);
    }

    private void drawChessboard(Graphics g) {
        g.setColor(GRID_COLOR);
        for (int i = 0; i < BOARD_SIZE; i++) {
            // 竖线
            g.drawLine(MARGIN + i * GRID_SIZE, MARGIN, MARGIN + i * GRID_SIZE, MARGIN + (BOARD_SIZE - 1) * GRID_SIZE);
            // 横线
            g.drawLine(MARGIN, MARGIN + i * GRID_SIZE, MARGIN + (BOARD_SIZE - 1) * GRID_SIZE, MARGIN + i * GRID_SIZE);
        }
    }

    private void drawPieces(Graphics g) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != 0) {
                    int x = MARGIN + j * GRID_SIZE - PIECE_SIZE / 2;
                    int y = MARGIN + i * GRID_SIZE - PIECE_SIZE / 2;
                    if (board[i][j] == 1) {
                        g.setColor(BLACK_PIECE);
                    } else {
                        g.setColor(WHITE_PIECE);
                    }
                    g.fillOval(x, y, PIECE_SIZE, PIECE_SIZE);
                    g.setColor(GRID_COLOR);
                    g.drawOval(x, y, PIECE_SIZE, PIECE_SIZE);
                }
            }
        }
    }

    private void makeMove(int row, int col) {
        if (board[row][col] != 0) return;

        int player = isBlackTurn ? 1 : 2;

        // 专业模式下检查黑棋禁手
        if (professionalMode && player == 1) {
            if (isForbiddenMove(row, col)) {
                JOptionPane.showMessageDialog(this, "黑棋禁手，无法落子！");
                return;
            }
        }

        board[row][col] = player;
        moveHistory.add(new int[]{row, col, player});
        timeHistory.add(new long[]{System.currentTimeMillis() - startTime});

        // 检查胜负
        int[] winLine = checkWin(row, col, player);
        if (winLine != null) {
            gameOver = true;
            String winner = player == 1 ? "黑方" : "白方";
            statusLabel.setText("游戏状态: " + winner + "获胜");
            statusLabel.setForeground(Color.RED);
            highlightWinLine(winLine);
            JOptionPane.showMessageDialog(this, winner + "获胜！", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 切换玩家
        isBlackTurn = !isBlackTurn;
        currentPlayerLabel.setText("当前执子: " + (isBlackTurn ? "黑方" : "白方"));
        chessboardPanel.repaint();
    }

    private boolean isForbiddenMove(int row, int col) {
        // 简单实现禁手规则：双三、双四、长连
        board[row][col] = 1; // 临时放置黑棋
        boolean forbidden = false;

        // 检查双三
        int threeCount = 0;
        if (countThree(row, col, 1)) threeCount++;
        if (countThree(row, col, 2)) threeCount++;
        if (countThree(row, col, 3)) threeCount++;
        if (countThree(row, col, 4)) threeCount++;
        if (threeCount >= 2) forbidden = true;

        // 检查双四
        int fourCount = 0;
        if (countFour(row, col, 1)) fourCount++;
        if (countFour(row, col, 2)) fourCount++;
        if (countFour(row, col, 3)) fourCount++;
        if (countFour(row, col, 4)) fourCount++;
        if (fourCount >= 2) forbidden = true;

        // 检查长连（超过5个）
        if (countLong(row, col)) forbidden = true;

        board[row][col] = 0; // 恢复空位
        return forbidden;
    }

    private boolean countThree(int row, int col, int direction) {
        // 检查指定方向是否形成活三
        int[] dx = {1, 1, 0, -1}; // 四个方向的x增量
        int[] dy = {0, 1, 1, 1}; // 四个方向的y增量
        int d = direction - 1;
        int count = 1;
        boolean open = true;

        // 向一个方向检查
        for (int i = 1; i < 4; i++) {
            int x = row + dx[d] * i;
            int y = col + dy[d] * i;
            if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                open = false;
                break;
            }
            if (board[x][y] == 1) {
                count++;
            } else if (board[x][y] == 0) {
                break;
            } else {
                open = false;
                break;
            }
        }

        // 向相反方向检查
        for (int i = 1; i < 4; i++) {
            int x = row - dx[d] * i;
            int y = col - dy[d] * i;
            if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                open = false;
                break;
            }
            if (board[x][y] == 1) {
                count++;
            } else if (board[x][y] == 0) {
                break;
            } else {
                open = false;
                break;
            }
        }

        return count == 3 && open;
    }

    private boolean countFour(int row, int col, int direction) {
        // 检查指定方向是否形成活四或冲四
        int[] dx = {1, 1, 0, -1}; // 四个方向的x增量
        int[] dy = {0, 1, 1, 1}; // 四个方向的y增量
        int d = direction - 1;
        int count = 1;
        int emptyCount = 0;

        // 向一个方向检查
        for (int i = 1; i < 5; i++) {
            int x = row + dx[d] * i;
            int y = col + dy[d] * i;
            if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                break;
            }
            if (board[x][y] == 1) {
                count++;
            } else if (board[x][y] == 0) {
                emptyCount++;
                break;
            } else {
                break;
            }
        }

        // 向相反方向检查
        for (int i = 1; i < 5; i++) {
            int x = row - dx[d] * i;
            int y = col - dy[d] * i;
            if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                break;
            }
            if (board[x][y] == 1) {
                count++;
            } else if (board[x][y] == 0) {
                emptyCount++;
                break;
            } else {
                break;
            }
        }

        return count == 4 && emptyCount >= 1;
    }

    private boolean countLong(int row, int col) {
        // 检查是否形成超过5个的连子
        for (int d = 0; d < 4; d++) {
            int count = 1;
            int[] dx = {1, 1, 0, -1}; // 四个方向
            int[] dy = {0, 1, 1, 1};
            for (int i = 0; i < 2; i++) {
                int step = (i == 0) ? 1 : -1;
                int x = row + dx[d] * step;
                int y = col + dy[d] * step;
                while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[x][y] == 1) {
                    count++;
                    x += dx[d] * step;
                    y += dy[d] * step;
                }
            }
            if (count > 5) return true;
        }
        return false;
    }

    private int[] checkWin(int row, int col, int player) {
        // 检查四个方向：横、竖、斜
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] dir : directions) {
            int count = 1;
            int startRow = row;
            int startCol = col;
            int endRow = row;
            int endCol = col;

            // 向一个方向延伸
            for (int i = 1; i < 5; i++) {
                int newRow = row + dir[0] * i;
                int newCol = col + dir[1] * i;
                if (newRow >= 0 && newRow < BOARD_SIZE && newCol >= 0 && newCol < BOARD_SIZE && board[newRow][newCol] == player) {
                    count++;
                    endRow = newRow;
                    endCol = newCol;
                } else {
                    break;
                }
            }

            // 向相反方向延伸
            for (int i = 1; i < 5; i++) {
                int newRow = row - dir[0] * i;
                int newCol = col - dir[1] * i;
                if (newRow >= 0 && newRow < BOARD_SIZE && newCol >= 0 && newCol < BOARD_SIZE && board[newRow][newCol] == player) {
                    count++;
                    startRow = newRow;
                    startCol = newCol;
                } else {
                    break;
                }
            }

            if (count >= 5) {
                return new int[]{startRow, startCol, endRow, endCol};
            }
        }
        return null;
    }

    private void highlightWinLine(int[] winLine) {
        // 高亮显示获胜的五子连线
        Graphics g = chessboardPanel.getGraphics();
        g.setColor(WIN_HIGHLIGHT);
        g.setStroke(new BasicStroke(3));
        int x1 = MARGIN + winLine[1] * GRID_SIZE;
        int y1 = MARGIN + winLine[0] * GRID_SIZE;
        int x2 = MARGIN + winLine[3] * GRID_SIZE;
        int y2 = MARGIN + winLine[2] * GRID_SIZE;
        g.drawLine(x1, y1, x2, y2);
    }

    private void undoMove() {
        if (moveHistory.isEmpty()) return;
        if (moveHistory.size() > 3) {
            JOptionPane.showMessageDialog(this, "最多只能悔三步！");
            return;
        }

        int[] lastMove = moveHistory.remove(moveHistory.size() - 1);
        board[lastMove[0]][lastMove[1]] = 0;
        isBlackTurn = !isBlackTurn;
        currentPlayerLabel.setText("当前执子: " + (isBlackTurn ? "黑方" : "白方"));
        statusLabel.setText("游戏状态: 进行中");
        statusLabel.setForeground(Color.BLUE);
        gameOver = false;
        chessboardPanel.repaint();
    }

    private void restartGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        isBlackTurn = true;
        gameOver = false;
        moveHistory.clear();
        timeHistory.clear();
        startTime = System.currentTimeMillis();
        currentPlayerLabel.setText("当前执子: 黑方");
        statusLabel.setText("游戏状态: 进行中");
        statusLabel.setForeground(Color.BLUE);
        chessboardPanel.repaint();
    }

    private void startTimer() {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameOver) return;
                long elapsed = System.currentTimeMillis() - startTime;
                int minutes = (int) (elapsed / 60000);
                int seconds = (int) ((elapsed % 60000) / 1000);
                timeLabel.setText(String.format("已用时长: %02d:%02d", minutes, seconds));
            }
        });
        timer.start();
    }
}