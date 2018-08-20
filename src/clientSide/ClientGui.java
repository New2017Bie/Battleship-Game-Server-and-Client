package clientSide;

import serverSide.GridStatus;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

// can change boolean isDeploymentVertical through JRadioButtons
// need to implement cell numbers and validation
public class ClientGui extends JFrame{

    private JTextArea taChat;
    private JTextArea taConsole;
    private String msg = "";
    private String username = "";
    private String host = "127.0.0.1";
    private GameAction gameAction;
    public enum GameAction {JOIN, EXIT, ATTEMPT, EMPTY}
    public boolean isDeploymentVertical = true;
    private static Map<GridStatus, Color> shipColor = new TreeMap<>();
    private Color currentShipColor = Color.WHITE;
    private GridStatus currentShip;
    private HashSet<GridStatus> currentShipOnBoard = new HashSet<>();
    private static final int BOARDSIZE = 10;
    private GridStatus[][] myBoard = new GridStatus[BOARDSIZE][BOARDSIZE];
    GridStatus[][] targetBoard = new GridStatus[BOARDSIZE][BOARDSIZE];
    private JButton[][] myButtons = new JButton[BOARDSIZE][BOARDSIZE];
    private JButton[][] targetButtons = new JButton[BOARDSIZE][BOARDSIZE];
    private final int CELLSIZE = 35;
    private final int WIDTH = 380;
    private int[] attempt = new int[2];
    private Audio audio;

    ClientGui() {
        setMinimumSize(new Dimension(2 * WIDTH + 10, 800));
        setPreferredSize(new Dimension(2 * WIDTH + 10, 800));

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0};
        gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE, 1.0};
        panel.setLayout(gbl_panel);

        JLabel jlTargetBoard = new JLabel("Target Board");
        jlTargetBoard.setMinimumSize(new Dimension(150, 50));
        jlTargetBoard.setPreferredSize(new Dimension(150, 50));
        jlTargetBoard.setVerticalAlignment(JLabel.CENTER);
        jlTargetBoard.setHorizontalAlignment(JLabel.CENTER);
        GridBagConstraints gbc_jlTargetBoard = new GridBagConstraints();
        gbc_jlTargetBoard.insets = new Insets(10, 0, 0, 0);
        gbc_jlTargetBoard.gridx = 0;
        gbc_jlTargetBoard.gridy = 0;
        panel.add(jlTargetBoard, gbc_jlTargetBoard);

        JPanel jpTargetBoard = new JPanel();
        jpTargetBoard.setMinimumSize(new Dimension(WIDTH, WIDTH + 5));
        jpTargetBoard.setPreferredSize(new Dimension(WIDTH, WIDTH + 5));
        GridBagConstraints gbc_jpTargetBoard = new GridBagConstraints();
        gbc_jpTargetBoard.insets = new Insets(5, 0, 5, 0);
        gbc_jpTargetBoard.fill = GridBagConstraints.BOTH;
        gbc_jpTargetBoard.gridx = 0;
        gbc_jpTargetBoard.gridy = 1;
        panel.add(jpTargetBoard, gbc_jpTargetBoard);

        jpTargetBoard.setLayout(null);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                targetButtons[i][j] = new JButton();
                targetButtons[i][j].setMinimumSize(new Dimension(CELLSIZE, CELLSIZE));
                targetButtons[i][j].setMaximumSize(new Dimension(CELLSIZE, CELLSIZE));
                targetButtons[i][j].setPreferredSize(new Dimension(CELLSIZE, CELLSIZE));
                targetButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                targetButtons[i][j].setBounds(10 + i * (CELLSIZE + 1), 5 + j * (CELLSIZE + 1), CELLSIZE, CELLSIZE);
                targetButtons[i][j].setBackground(Color.WHITE);
                jpTargetBoard.add(targetButtons[i][j]);

                targetButtons[i][j].addActionListener(new targetActionListener(i, j));
            }
        }

        JScrollPane spChat = new JScrollPane();
        spChat.setBorder(new EmptyBorder(10, 10, 10, 10));
        spChat.setMinimumSize(new Dimension(WIDTH, 200));
        spChat.setPreferredSize(new Dimension(WIDTH, 200));
        spChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        GridBagConstraints gbc_spChat = new GridBagConstraints();
        gbc_spChat.insets = new Insets(0, 0, 0, 0);
        gbc_spChat.fill = GridBagConstraints.BOTH;
        gbc_spChat.gridx = 0;
        gbc_spChat.gridy = 2;
        panel.add(spChat, gbc_spChat);

        taChat = new JTextArea();
        taChat.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        spChat.setViewportView(taChat);
        taChat.requestFocus();
        taChat.setLineWrap(true);

        taChat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {
                    sendText();
                }
            }
        });

        taConsole = new JTextArea();
        taConsole.setEditable(false);
        taConsole.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        taConsole.setLineWrap(true);
        taConsole.setFont(new Font("courier", Font.PLAIN, 12));

        JPanel panel_btn = new JPanel();
        panel_btn.setMinimumSize(new Dimension(WIDTH, 260));
        panel_btn.setPreferredSize(new Dimension(WIDTH, 260));
        panel_btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc_panel_btn = new GridBagConstraints();
        gbc_panel_btn.fill = GridBagConstraints.BOTH;
        gbc_panel_btn.gridx = 0;
        gbc_panel_btn.gridy = 3;
        panel.add(panel_btn, gbc_panel_btn);
        GridBagLayout gbl_panel_btn = new GridBagLayout();
        gbl_panel_btn.columnWidths = new int[]{0, 0, 0, 0};
        gbl_panel_btn.rowHeights = new int[]{0, 0};
        gbl_panel_btn.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panel_btn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel_btn.setLayout(gbl_panel_btn);

        JButton btnChat = new JButton("Chat");
        btnChat.setPreferredSize(new Dimension(80, 25));
        btnChat.setMinimumSize(new Dimension(80, 25));
        btnChat.setMaximumSize(new Dimension(80, 25));
        GridBagConstraints gbc_btnChat = new GridBagConstraints();
        gbc_btnChat.insets = new Insets(10, 10, 10, 10 );
        gbc_btnChat.gridx = 0;
        gbc_btnChat.gridy = 0;
        panel_btn.add(btnChat, gbc_btnChat);

        JButton btnJoin = new JButton("Join");
        btnJoin.setPreferredSize(new Dimension(80, 25));
        btnJoin.setMinimumSize(new Dimension(80, 25));
        btnJoin.setMaximumSize(new Dimension(80, 25));
        GridBagConstraints gbc_btnJoin = new GridBagConstraints();
        gbc_btnJoin.insets = new Insets(10, 10, 10, 10 );
        gbc_btnJoin.gridx = 1;
        gbc_btnJoin.gridy = 0;
        panel_btn.add(btnJoin, gbc_btnJoin);
        btnJoin.addActionListener(e -> join());

        JButton btnExit = new JButton("Exit");
        btnExit.setPreferredSize(new Dimension(80, 25));
        btnExit.setMinimumSize(new Dimension(80, 25));
        btnExit.setMaximumSize(new Dimension(80, 25));
        GridBagConstraints gbc_btnExit = new GridBagConstraints();
        gbc_btnExit.insets = new Insets(10, 10, 10, 10 );
        gbc_btnExit.gridx = 2;
        gbc_btnExit.gridy = 0;
        panel_btn.add(btnExit, gbc_btnExit);

        btnChat.addActionListener(e -> sendText());
        btnJoin.addActionListener(e -> setGameAction(GameAction.JOIN));
        btnExit.addActionListener(e -> setGameAction(GameAction.EXIT));

        btnChat.setBackground(Color.BLACK);
        btnJoin.setBackground(Color.BLACK);
        btnExit.setBackground(Color.BLACK);

        btnChat.setForeground(Color.WHITE);
        btnJoin.setForeground(Color.WHITE);
        btnExit.setForeground(Color.WHITE);

        JPanel jpMyBoard = new JPanel();
        jpMyBoard.setMinimumSize(new Dimension(WIDTH, WIDTH + 5));
        jpMyBoard.setPreferredSize(new Dimension(WIDTH, WIDTH + 5));
        GridBagConstraints gbc_jpBoard = new GridBagConstraints();
        gbc_jpBoard.insets = new Insets(5, 0, 5, 0);
        gbc_jpBoard.fill = GridBagConstraints.BOTH;
        gbc_jpBoard.gridx = 1;
        gbc_jpBoard.gridy = 1;
        panel.add(jpMyBoard, gbc_jpBoard);

        jpMyBoard.setLayout(null);


        JLabel jlMyBoard = new JLabel("My Board");
        jlMyBoard.setMinimumSize(new Dimension(150, 50));
        jlMyBoard.setPreferredSize(new Dimension(150, 50));
        jlMyBoard.setVerticalAlignment(JLabel.CENTER);
        jlMyBoard.setHorizontalAlignment(JLabel.CENTER);
        GridBagConstraints gbc_jlMyBoard = new GridBagConstraints();
        gbc_jlMyBoard.insets = new Insets(10, 0, 0, 0);
        gbc_jlMyBoard.gridx = 1;
        gbc_jlMyBoard.gridy = 0;
        panel.add(jlMyBoard, gbc_jlMyBoard);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                myButtons[i][j] = new JButton();
                myButtons[i][j].setMinimumSize(new Dimension(CELLSIZE, CELLSIZE));
                myButtons[i][j].setMaximumSize(new Dimension(CELLSIZE, CELLSIZE));
                myButtons[i][j].setPreferredSize(new Dimension(CELLSIZE, CELLSIZE));
                myButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                myButtons[i][j].setBounds(10 + i * (CELLSIZE + 1), 5 + j * (CELLSIZE + 1), CELLSIZE, CELLSIZE);
                myButtons[i][j].setBackground(Color.WHITE);
                jpMyBoard.add(myButtons[i][j]);

                myButtons[i][j].addActionListener(new myBoardActionListener(i, j));
            }
        }

        JScrollPane spConsole = new JScrollPane();
        spConsole.setBorder(new EmptyBorder(10, 10, 10, 10));
        spConsole.setMinimumSize(new Dimension(WIDTH, 200));
        spConsole.setPreferredSize(new Dimension(WIDTH, 200));
        spConsole.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spConsole.setViewportView(taConsole);
        GridBagConstraints gbc_spConsole = new GridBagConstraints();
        gbc_spConsole.insets = new Insets(0, 0, 0, 0);
        gbc_spConsole.fill = GridBagConstraints.BOTH;
        gbc_spConsole.gridx = 1;
        gbc_spConsole.gridy = 2;
        panel.add(spConsole, gbc_spConsole);

        JPanel jpShipButton = new JPanel();
        jpShipButton.setMinimumSize(new Dimension(WIDTH, 260));
        jpShipButton.setPreferredSize(new Dimension(WIDTH, 260));
        GridBagLayout gbl_jpShipButton = new GridBagLayout();
        gbl_jpShipButton.columnWidths = new int[]{0, 0};
        gbl_jpShipButton.rowHeights = new int[]{0, 0, 0};
        gbl_jpShipButton.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_jpShipButton.rowWeights = new double[]{1.0, Double.MIN_VALUE, 1.0};
        jpShipButton.setLayout(gbl_jpShipButton);
        GridBagConstraints gbc_jpButton = new GridBagConstraints();
        gbc_jpButton.insets = new Insets(0, 0, 5, 0);
        gbc_jpButton.fill = GridBagConstraints.BOTH;
        gbc_jpButton.gridx = 1;
        gbc_jpButton.gridy = 3;
        panel.add(jpShipButton, gbc_jpButton);

        JButton btnCarrier = new JButton("Carrier");
        btnCarrier.setPreferredSize(new Dimension(120, 25));
        btnCarrier.setMinimumSize(new Dimension(120, 25));
        btnCarrier.setMaximumSize(new Dimension(120, 25));
        btnCarrier.setBackground(Color.BLACK);
        btnCarrier.setForeground(Color.WHITE);
        GridBagConstraints gbc_btnCarrier = new GridBagConstraints();
        gbc_btnCarrier.insets = new Insets(10, 0, 10, 5);
        gbc_btnCarrier.gridx = 0;
        gbc_btnCarrier.gridy = 0;
        jpShipButton.add(btnCarrier, gbc_btnCarrier);

        JButton btnDestroyer = new JButton("Destroyer");
        btnDestroyer.setPreferredSize(new Dimension(120, 25));
        btnDestroyer.setMinimumSize(new Dimension(120, 25));
        btnDestroyer.setMaximumSize(new Dimension(120, 25));
        btnDestroyer.setBackground(Color.BLUE);
        btnDestroyer.setForeground(Color.WHITE);
        GridBagConstraints gbc_btnDestroyer = new GridBagConstraints();
        gbc_btnDestroyer.insets = new Insets(5, 0, 5, 20);
        gbc_btnDestroyer.gridx = 1;
        gbc_btnDestroyer.gridy = 0;
        jpShipButton.add(btnDestroyer, gbc_btnDestroyer);

        JButton btnBattleship = new JButton("Battleship");
        btnBattleship.setPreferredSize(new Dimension(120, 25));
        btnBattleship.setMinimumSize(new Dimension(120, 25));
        btnBattleship.setMaximumSize(new Dimension(120, 25));
        btnBattleship.setBackground(Color.GREEN);
        btnBattleship.setForeground(Color.BLACK);
        GridBagConstraints gbc_btnBattleship = new GridBagConstraints();
        gbc_btnBattleship.insets = new Insets(5, 0, 5, 20);
        gbc_btnBattleship.gridx = 1;
        gbc_btnBattleship.gridy = 1;
        jpShipButton.add(btnBattleship, gbc_btnBattleship);

        JButton btnCruiser = new JButton("Cruiser");
        btnCruiser.setPreferredSize(new Dimension(120, 25));
        btnCruiser.setMinimumSize(new Dimension(120, 25));
        btnCruiser.setMaximumSize(new Dimension(120, 25));
        btnCruiser.setBackground(Color.YELLOW);
        GridBagConstraints gbc_btnCruiser = new GridBagConstraints();
        gbc_btnCruiser.insets = new Insets(5, 0, 5, 5);
        gbc_btnCruiser.gridx = 0;
        gbc_btnCruiser.gridy = 1;
        jpShipButton.add(btnCruiser, gbc_btnCruiser);

        JButton btnSubmarine  = new JButton("Submarine");
        btnSubmarine.setPreferredSize(new Dimension(120, 25));
        btnSubmarine.setMinimumSize(new Dimension(120, 25));
        btnSubmarine.setMaximumSize(new Dimension(120, 25));
        btnSubmarine.setBackground(Color.cyan);
        GridBagConstraints gbc_btnSubmarine = new GridBagConstraints();
        gbc_btnSubmarine.insets = new Insets(5, 0, 5, 5);
        gbc_btnSubmarine.gridx = 0;
        gbc_btnSubmarine.gridy = 2;
        jpShipButton.add(btnSubmarine, gbc_btnSubmarine);

        JButton btnRemove  = new JButton("Remove");
        btnRemove.setPreferredSize(new Dimension(120, 25));
        btnRemove.setMinimumSize(new Dimension(120, 25));
        btnRemove.setMaximumSize(new Dimension(120, 25));
        btnRemove.setBackground(Color.WHITE);
        btnRemove.setForeground(Color.BLACK);
        GridBagConstraints gbc_btnRemove = new GridBagConstraints();
        gbc_btnRemove.insets = new Insets(5, 0, 5, 20);
        gbc_btnRemove.gridx = 1;
        gbc_btnRemove.gridy = 2;
        jpShipButton.add(btnRemove, gbc_btnRemove);

        JRadioButton jrVertical = new JRadioButton("Vertical", true);
        JRadioButton jrHorizontal = new JRadioButton("Horizontal");

        ButtonGroup bgDirection = new ButtonGroup();
        bgDirection.add(jrVertical);
        bgDirection.add(jrHorizontal);
        GridBagConstraints gbc_jrVertical = new GridBagConstraints();
        gbc_jrVertical.insets = new Insets(5, 0, 20, 5);
        gbc_jrVertical.gridx = 0;
        gbc_jrVertical.gridy = 3;
        jpShipButton.add(jrVertical, gbc_jrVertical);

        GridBagConstraints gbc_jrHorizontal = new GridBagConstraints();
        gbc_jrHorizontal.insets = new Insets(5, 0, 20, 20);
        gbc_jrHorizontal.gridx = 1;
        gbc_jrHorizontal.gridy = 3;
        jpShipButton.add(jrHorizontal, gbc_jrHorizontal);

        jrVertical.addActionListener(e -> isDeploymentVertical = jrVertical.isSelected());
        jrHorizontal.addActionListener(e -> isDeploymentVertical = !jrHorizontal.isSelected());

        btnSubmarine.addActionListener(e -> setCurrentShip(GridStatus.Submarine));
        btnCarrier.addActionListener(e -> setCurrentShip(GridStatus.Carrier));
        btnDestroyer.addActionListener(e -> setCurrentShip(GridStatus.Destroyer));
        btnBattleship.addActionListener(e -> setCurrentShip(GridStatus.Battleship));
        btnCruiser.addActionListener(e -> setCurrentShip(GridStatus.Cruiser));
        btnRemove.addActionListener(e -> setCurrentShip(GridStatus.EMPTY));

        AssignShip();
        inputHost();
        inputUsername();
        enableOrDisableOpponentBoardInput(false);
        enableOrDisableOpponentBoardInput(true);
        initializeBoards();
        playWelcomeAudio();

        this.setLocation(100, 100);
        this.setTitle("Battleship");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        panel.setVisible(true);
        this.setVisible(true);
        this.pack();
    }

    void playWelcomeAudio() {
        audio = new Audio("welcome.wav");
        audio.play();
    }

    void playFireAudio() {
        audio = new Audio("fire.wav");
        audio.play();
    }

    private void initializeBoards() {
        for (int i = 0; i < BOARDSIZE; i++) {
            Arrays.fill(getMyBoard()[i], GridStatus.EMPTY);
            Arrays.fill(getTargetBoard()[i], GridStatus.EMPTY);
        }
    }

    private boolean validateBoard() {
        HashSet<GridStatus> shipsOnBoard = new HashSet<>();
        for (int i = 0; i < BOARDSIZE; i++) {
            for (int j = 0; j < BOARDSIZE; j++) {
                if (myBoard[i][j].isShip()) {
                    shipsOnBoard.add(myBoard[i][j]);
                }
            }
        }

        if (shipsOnBoard.size() != 5) {
            return false;
        }

        return true;
    }

    // finished ship inputting, and ready to start a game
    private void join() {
        if (validateBoard()) {
            enableOrDisableMyBoardInput(false);
        } else {
            warning("Incomplete game board.");
        }
    }

    void enableOrDisableMyBoardInput(boolean isEnable) {
        for (int i = 0; i < BOARDSIZE; i++) {
            for (int j = 0; j < BOARDSIZE; j++) {
                myButtons[i][j].setEnabled(isEnable);
            }
        }
    }

    void enableOrDisableOpponentBoardInput(boolean isEnable) {
        for (int i = 0; i < BOARDSIZE; i++) {
            for (int j = 0; j < BOARDSIZE; j++) {
                targetButtons[i][j].setEnabled(isEnable);
            }
        }
    }

    private void AssignShip() {
        shipColor.put(GridStatus.Battleship, Color.GREEN);
        shipColor.put(GridStatus.Submarine, Color.cyan);
        shipColor.put(GridStatus.Cruiser, Color.yellow);
        shipColor.put(GridStatus.Carrier, Color.BLACK);
        shipColor.put(GridStatus.Destroyer, Color.BLUE);
        shipColor.put(GridStatus.EMPTY, Color.WHITE);
        shipColor.put(GridStatus.MISS, Color.LIGHT_GRAY);
        shipColor.put(GridStatus.HIT, Color.RED);
    }

    void showMsg(String msg) {
        taConsole.append(msg);
        taConsole.setCaretPosition(taConsole.getDocument().getLength());
    }

    void showMsgln(String msg) {
        taConsole.append(msg + "\n");
        taConsole.setCaretPosition(taConsole.getDocument().getLength());
    }

    private Color getCurrentShipColor() {
        return currentShipColor;
    }

    private void setCurrentShip(GridStatus ship) {
        if (ship.isShip() || ship.equals(GridStatus.EMPTY)) {
            this.currentShipColor = shipColor.get(ship);
            this.currentShip = ship;
        }
    }

    String getMsg() {
        return msg;
    }

    void setMsg(String msg) {
        this.msg = msg;
    }

    private void sendText() {
        if (taChat.getText() != null) {
            setMsg(taChat.getText());
            taChat.setText("");
        }
    }

    GameAction getGameAction() {
        return gameAction;
    }

    void setGameAction(GameAction gameAction) {

        if (gameAction.equals(GameAction.JOIN)) {
            if (validateBoard()) {
                this.gameAction = gameAction;
            }
        } else {
            this.gameAction = gameAction;
        }
    }

    String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    int[] getAttempt() {
        return attempt;
    }

    void setAttempt(int[] attempt) {
        this.attempt = attempt;
    }

    void repaintMyBoard() {
        for (int row = 0; row < BOARDSIZE; row++) {
            for (int col = 0; col < BOARDSIZE; col++) {
                GridStatus tmpStatus = myBoard[col][row];
                Color tmpColor = shipColor.get(tmpStatus);
                myButtons[col][row].setBackground (tmpColor);
            }
        }
    }

    void repaintTargetBoard() {
        for (int row = 0; row < BOARDSIZE; row++) {
            for (int col = 0; col < BOARDSIZE; col++) {
                GridStatus tmpStatus = targetBoard[col][row];
                Color tmpColor = shipColor.get(tmpStatus);
                targetButtons[col][row].setBackground (tmpColor);
            }
        }
    }

    private void inputUsername() {
        setUsername(JOptionPane.showInputDialog(null, "Please enter the username:", "Bo"));
    }

    private void inputHost() {
        setHost(JOptionPane.showInputDialog(null, "Please enter the" +
                "Host Ip: ", "127.0.0.1"));
    }

    void exitWarning() {
        JOptionPane.showMessageDialog(this, "Disconnected. The program will exit.");
    }

    void warning(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    GridStatus[][] getMyBoard() {
        return myBoard;
    }

    void setMyBoard(GridStatus[][] mb) {
        this.myBoard = mb;
    }

    private void changeMyBoard(GridStatus value, int col, int row) {
        myBoard[col][row] = value;
    }

    private GridStatus[][] getTargetBoard() {
        return targetBoard;
    }

    public GridStatus getCurrentShip() {
        return currentShip;
    }

    public boolean isDeploymentVertical() {
        return isDeploymentVertical;
    }

    public JButton[][] getMyButtons() {
        return myButtons;
    }

    public HashSet<GridStatus> getCurrentShipOnBoard() {
        return currentShipOnBoard;
    }

    public void addCurrentShipOnBoard(GridStatus ship) {
        this.currentShipOnBoard.add(ship);
    }

    // ActionListener for the buttons on targetBoard
    private class targetActionListener implements ActionListener {
        private int col, row;

        targetActionListener(int col, int row) {
            this.col = col;
            this.row = row;
        }

        public void actionPerformed(ActionEvent e) {
            if (gameAction.equals(GameAction.EMPTY)) {
                setAttempt(new int[]{col, row});
                gameAction = GameAction.ATTEMPT;
            } else {
                warning("Not your turn.");
            }
        }
    }

    private class myBoardActionListener implements ActionListener {
        private int col, row;
        private JButton[][] jButtons;

        myBoardActionListener(int col, int row) {
            this.col = col;
            this.row = row;
            this.jButtons = getMyButtons();
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println(getCurrentShip());
            if (getCurrentShip().isShip()) {
                setShip();
            } else if (getCurrentShip().equals(GridStatus.EMPTY)) {
                GridStatus ship = getMyBoard()[col][row];
                if (ship.isShip()) {
                    getCurrentShipOnBoard().remove(ship);
                    for (int i = 0; i < BOARDSIZE; i++) {
                        for (int j = 0; j < BOARDSIZE; j++) {
                            if (getMyBoard()[j][i].equals(ship)) {
                                getMyBoard()[j][i] = GridStatus.EMPTY;
                                getMyButtons()[j][i].setBackground(Color.WHITE);
                            }
                        }
                    }
                }
            }
        }

        private void setShip() {
            GridStatus ship = getCurrentShip();
            int shipCells = ship.getLife();
            Color color = getCurrentShipColor();

            if (isDeploymentVertical()) {
                if (col + shipCells <= 10) {
                    for (int i = 0; i < shipCells; i++) {
                        if (!this.jButtons[col + i][row].getBackground().equals(Color.WHITE)) {
                            warning("Invalid deployment!");
                            return;
                        }
                    }

                    int previousSize = getCurrentShipOnBoard().size();
                    addCurrentShipOnBoard(ship);
                    int currentSize = getCurrentShipOnBoard().size();
                    if ((previousSize + 1) != currentSize) {
                        warning("Already deployed this ship.");
                        return;
                    }

                    for (int i = 0; i < shipCells; i++) {
                        this.jButtons[col + i][row].setBackground(color);

                        shipDeployment(color, col + i, row);
                    }
                } else {
                    warning("Invalid deployment!");
                }
            } else {
                if (row + shipCells <= 10) {
                    for (int i = 0; i < shipCells; i++) {
                        if (!this.jButtons[col][row + i].getBackground().equals(Color.WHITE)) {
                            warning("Invalid deployment!");
                            return;
                        }
                    }

                    int previousSize = getCurrentShipOnBoard().size();
                    addCurrentShipOnBoard(ship);
                    int currentSize = getCurrentShipOnBoard().size();
                    if ((previousSize + 1) != currentSize) {
                        warning("Already deployed this ship.");
                        return;
                    }

                    for (int i = 0; i < shipCells; i++) {
                        this.jButtons[col][row + i].setBackground(color);

                        shipDeployment(color, col, row + i);
                    }
                } else {
                    warning("Invalid deployment!");
                }
            }
        }

        private void shipDeployment(Color color, int i, int row) {
            if (color.equals(Color.BLUE)) {
                changeMyBoard(GridStatus.Destroyer, i, row);
            } else if (color.equals(Color.cyan)) {
                changeMyBoard(GridStatus.Submarine, i, row);
            } else if (color.equals(Color.YELLOW)) {
                changeMyBoard(GridStatus.Cruiser, i, row);
            } else if (color.equals(Color.GREEN)) {
                changeMyBoard(GridStatus.Battleship, i, row);
            } else if (color.equals(Color.BLACK)) {
                changeMyBoard(GridStatus.Carrier, i, row);
            } else if (color.equals(Color.WHITE)) {
                changeMyBoard(GridStatus.EMPTY, i, row);
            }
        }
    }

    public static void main (String[] args) {
        new ClientGui();
    }
}