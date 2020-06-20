import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class GUI extends JFrame{

    public static void main(String args[]){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
    }

    //game GUI
    private JFrame game;
    private JButton[][] board;

    private JMenuBar menuBar;
    private JComboBox difficulty;
    private String difficultyText = "easy";
    private String currentDifficulty = difficultyText;
    private JButton sound;

    private JLabel flags;

    private JLabel time;
    private int seconds = 0;
    private int[] bestTimes = {0, 0, 0};
    private int timeIndex;

    private String[][] solved = createBoard(); //places bombs

    private Timer timer;

    //gameover GUI
    private JFrame frame;
    private JPanel panel;
    private JButton playAgain;
    private JLabel result;
    private JLabel times;

    //other variables
    private boolean gameover = false;
    private boolean soundEffects = true;

    private int turn = 0;
    private int revealed;
    private int totalMines;
    private int flagsLeft;

    private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    //colors
    Color lightGreen = new Color(210, 255, 148);
    Color darkGreen = new Color(186, 227, 129);
    Color lightBrown = new Color(232, 197, 144);
    Color darkBrown = new Color(199, 169, 123);
    Color one = new Color(50, 90, 191);
    Color two = new Color(27, 107, 36);
    Color three = new Color(240, 78, 70);
    Color four = new Color(110, 40, 176);
    Color five = new Color(247, 117, 57);
    Color six = new Color(43, 143, 173);
    Color seven = new Color(166, 108, 149);
    Color eight = new Color(107, 74, 39);

    //sounds
    File click = new File("src/sounds/click.wav");
    File mine = new File("src/sounds/mine.wav");
    File flag = new File("src/sounds/flag.wav");
    File win = new File("src/sounds/win.wav");

    //images
    ImageIcon mineIcon = new ImageIcon("src/images/mine_icon.png");
    ImageIcon flagIcon = new ImageIcon("src/images/flag_icon.png");

    public GUI(){
        super();

        //game GUI
        game = new JFrame("Minesweeper");
        game.setLayout(new GridLayout(solved.length, solved.length));
        game.setSize(720, 737);
        game.setResizable(false);
        game.setDefaultCloseOperation(EXIT_ON_CLOSE);
        game.setLocation(dim.width/2-game.getSize().width/2, dim.height/2-game.getSize().height/2);
        game.setVisible(true);

        game.setFocusable(true);
        game.requestFocusInWindow();
        game.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                int keyCode = e.getKeyCode();
                if(keyCode == KeyEvent.VK_R){
                    newGame();
                }
            }
        });

        board = new JButton[solved.length][solved.length];

        //gameover GUI
        frame = new JFrame("Minesweeper");
        frame.setSize(300, 100);
        frame.setResizable(false);

        result = new JLabel("Result");
        result.setHorizontalAlignment(JLabel.CENTER);

        times = new JLabel("Time: "+seconds+"   Best Time: "+bestTimes[timeIndex]);
        times.setHorizontalAlignment(JLabel.CENTER);

        playAgain = new JButton("Play Again");
        playAgain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("playAgain pressed");
                newGame();
                frame.setVisible(false);
            }
        });

        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 0, 0));
        panel.add(result);
        panel.add(times);
        panel.add(playAgain);

        frame.add(panel);
        frame.setVisible(false);

        //initialize stuff
        initializeBoard();
        resetBoard();
        initializeMenuBar();
    }

    private void initializeMenuBar(){
        menuBar = new JMenuBar();

        difficulty = new JComboBox();
        difficulty.addItem("Easy (8 x 8)");
        difficulty.addItem("Medium (16 x 16)");
        difficulty.addItem("Hard (24 x 24)");
        difficulty.setMaximumSize(difficulty.getPreferredSize());
        difficulty.setFocusable(false);
        difficulty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                difficultyText = ((String) difficulty.getSelectedItem()).toLowerCase();

                if(!currentDifficulty.equals(difficultyText)){
                    newGame();
                }
            }
        });

        sound = new JButton("Sound Off");
        sound.setFocusable(false);
        sound.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSound();
            }
        });

        time = new JLabel("Time: "+seconds);
        time.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));

        flags = new JLabel("||  Flags Left: "+flagsLeft);
        flags.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        menuBar.add(difficulty);
        menuBar.add(time);
        menuBar.add(flags);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(sound);

        game.setJMenuBar(menuBar);
    }

    private void resetBoard(){
        gameover = false;

        solved = createBoard();
        findNeighbors(solved);

        if(!currentDifficulty.equals(difficultyText)){
            currentDifficulty = difficultyText;
            game.getContentPane().removeAll();
            game.setLayout(new GridLayout(solved.length, solved.length));
            board = new JButton[solved.length][solved.length];
            initializeBoard();
        }

        for(int i = 0; i < solved.length; i++){
            for(int j = 0; j < solved.length; j++){
                board[i][j].setText("");
                board[i][j].setIcon(null);

                if((i + j) % 2 == 0){
                    board[i][j].setBackground(lightGreen);

                }else{
                    board[i][j].setBackground(darkGreen);
                }
            }
        }
    }

    private void initializeBoard(){
        setTimer();

        for(int i = 0; i < solved.length; i++){
            for(int j = 0; j < solved.length; j++){
                JButton button = new JButton();
                button.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
                button.setMargin(new Insets(0, -20, 0, -20));
                board[i][j] = button;
                button.setOpaque(true);
                button.setBorderPainted(false);
                button.setSize(button.getPreferredSize());

                if((i + j) % 2 == 0){
                    button.setBackground(lightGreen);

                }else{
                    button.setBackground(darkGreen);
                }

                int i2 = i;
                int j2 = j;

                button.setFocusable(false);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if ((ActionEvent.CTRL_MASK & e.getModifiers()) != 0){
                            placeFlag(i2, j2);
                        }else{
                            if(((JButton) e.getSource()).getText().equals("") && gameover == false){
                                if(soundEffects){
                                    playSound(click);
                                }

                                if(turn == 0){
                                    turn++;
                                    while(!solved[i2][j2].equals("0")){
                                        resetBoard();
                                    }
                                    printBoard(solved);
                                    timer.start();
                                }
                                reveal(i2, j2);
                                checkWin();
                            }else{
                                System.out.println("already revealed");
                            }
                        }
                    }
                });

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        super.mousePressed(e);
                        if(e.getButton() == 3){
                            System.out.println("right");
                        }
                    }
                });

                game.add(button);
            }
        }
    }

    private String[][] createBoard(){
        int mines;
        String[][] board;

        if(difficultyText.equals("easy")){
            board = new String[8][8];
            mines = 10;
            totalMines = mines;
            flagsLeft = mines;
        }else if(difficultyText.equals("medium")){
            board = new String[16][16];
            mines = 40;
            totalMines = mines;
            flagsLeft = mines;
        }else{
            board = new String[24][24];
            mines = 99;
            totalMines = mines;
            flagsLeft = mines;
        }

        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                board[i][j] = "-";
            }
        }

        while(mines > 0){
            for(int i = 0; i < board.length; i++){
                for(int j = 0; j < board.length; j++){
                    double randChance = Math.random();
                    if(!board[i][j].equals("B") && mines > 0){
                        if(randChance <= .15){
                            board[i][j] = "B";
                            mines--;
                        }
                    }
                }
            }
        }

        return board;
    }

    private void findNeighbors(String[][] board){
        int mines = 0;

        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(!board[i][j].equals("B")){
                    for(int xOff = -1; xOff <= 1; xOff++){
                        for(int yOff = -1; yOff <= 1; yOff++){
                            int x = j + xOff;
                            int y = i + yOff;

                            if(x > -1 && x < board.length && y > -1 && y < board.length){
                                if(board[y][x].equals("B")){
                                    mines++;
                                }
                            }
                        }
                    }
                    board[i][j] = Integer.toString(mines);
                    mines = 0;
                }
            }
        }
    }

    private void printBoard(String[][] board){
        System.out.println("BOARD: ");

        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                System.out.print(board[i][j]+" ");
            }
            System.out.println("");
        }
    }

    private void reveal(int i, int j){
        if(!solved[i][j].equals("B")){
            board[i][j].setText(solved[i][j]);
        }

        //set background color
        if((i + j) % 2 == 0){
            board[i][j].setBackground(lightBrown);
        }else{
            board[i][j].setBackground(darkBrown);
        }

        //set number color
        String value = solved[i][j];
        switch(value){
            case "B":
                board[i][j].setForeground(Color.BLACK);
                board[i][j].setIcon(mineIcon);
                Image image = mineIcon.getImage();
                Image newImage = image.getScaledInstance(board[i][j].getWidth()/2, board[i][j].getHeight()/2, Image.SCALE_SMOOTH);
                board[i][j].setIcon(new ImageIcon(newImage));

                if(!gameover){
                    timer.stop();
                    if(soundEffects){
                        playSound(mine);
                    }
                    gameover = true;
                    System.out.println("<<Gameover>>");

                    showSolution();
                    findTimeIndex();

                    frame.setLocationRelativeTo(game);
                    result.setText("You lost");
                    if(bestTimes[timeIndex] > 0){
                        times.setText("Best Time: "+bestTimes[timeIndex]);
                    }else{
                        times.setText("Best Time: ---");
                    }

                    frame.setVisible(true);
                }
                break;
            case "0":
                if((i + j) % 2 == 0){
                    board[i][j].setForeground(lightBrown);
                }else{
                    board[i][j].setForeground(darkBrown);
                }
                break;
            case "1":
                board[i][j].setForeground(one);
                break;
            case "2":
                board[i][j].setForeground(two);
                break;
            case "3":
                board[i][j].setForeground(three);
                break;
            case "4":
                board[i][j].setForeground(four);
                break;
            case "5":
                board[i][j].setForeground(five);
                break;
            case "6":
                board[i][j].setForeground(six);
                break;
            case "7":
                board[i][j].setForeground(seven);
                break;
            case "8":
                board[i][j].setForeground(eight);
                break;
        }

        if(!gameover){
            for(int xOff = -1; xOff <= 1; xOff++){
                for(int yOff = -1; yOff <= 1; yOff++){
                    int x = j + xOff;
                    int y = i + yOff;

                    if(x > -1 && x < board.length && y > -1 && y < board.length){
                        String neighbor = solved[y][x];

                        if(!neighbor.equals("B") && solved[i][j].equals("0") && board[y][x].getText().equals("")){
                            reveal(y, x);
                        }
                    }
                }
            }
        }
        revealed++;
    }

    private void placeFlag(int i, int j){
        JButton cell = board[i][j];

        if(cell.getText().equals("")){
            if(soundEffects){
                playSound(flag);
            }
            cell.setText("P");
            cell.setForeground(Color.BLACK);
            flagsLeft--;
        }else if(cell.getText().equals("P")){
            if(soundEffects){
                playSound(flag);
            }
            cell.setText("");
            flagsLeft++;
        }

        flags.setText("||  Flags Left: "+flagsLeft);
    }

    private void checkWin(){
        int size = (int)Math.pow(board.length, 2);

        if(size - revealed == totalMines){
            timer.stop();

            if(soundEffects){
                playSound(win);
            }

            gameover = true;
            System.out.println("<<You won>>");

            showSolution();
            findTimeIndex();

            if(seconds < bestTimes[timeIndex] || bestTimes[timeIndex] == 0){
                bestTimes[timeIndex] = seconds;
            }

            frame.setLocationRelativeTo(game);
            result.setText("You won!");
            times.setText("Time: "+seconds+"   Best Time: "+bestTimes[timeIndex]);
            frame.setVisible(true);

        }
    }

    private void showSolution(){
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j].getText().equals("")){
                    reveal(i, j);
                }else if(board[i][j].getText().equals("F") && !solved[i][j].equals("B")){
                    reveal(i, j);
                }
            }
        }
    }

    private void setTimer(){
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seconds++;
                time.setText("Time: "+seconds);
            }
        });
    }

    private void playSound(File sound){
        try{
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(sound));
            clip.start();

        }catch(Exception e){
            System.out.println("sound \""+sound.getName()+"\" was not found");
        }
    }

    private void toggleSound() {
        if(soundEffects){
            soundEffects = false;
            sound.setText("Sound On");
        }else{
            soundEffects = true;
            sound.setText("Sound Off");
        }
    }

    private void newGame(){
        System.out.println("\n<<New Game>>");
        timer.stop();
        seconds = 0;
        time.setText("Time: "+seconds);

        turn = 0;
        revealed = 0;

        difficultyText = ((String) difficulty.getSelectedItem()).toLowerCase();
        int space = difficultyText.indexOf(" ");
        difficultyText = difficultyText.substring(0, space);

        System.out.println("difficulty: "+difficultyText);

        if(difficultyText.equals("easy")){
            flags.setText("||  Flags Left: 10");
        }else if(difficultyText.equals("medium")){
            flags.setText("||  Flags Left: 40");
        }else{
            flags.setText("||  Flags Left: 99");
        }
        resetBoard();
    }

    private void findTimeIndex(){
        //0 is easy, 1 is medium, 2 is hard
        if(difficultyText.equals("easy")){
            timeIndex = 0;
        }else if(difficultyText.equals("medium")){
            timeIndex = 1;
        }else{
            timeIndex = 2;
        }
    }
}