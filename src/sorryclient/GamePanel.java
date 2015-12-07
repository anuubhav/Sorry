package sorryclient;

import customUI.PaintedButton;
import customUI.PaintedPanel;
import game.GameHelpers;
import game.GameManager;
import game.Tile;
import library.FontLibrary;
import library.ImageLibrary;
import networking.MessageConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Stack;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1593344194657112518L;
	
	private final static int boardSize = 16;
	private final TilePanel[][] tileGrid;
	
	private final PaintedButton cardButton;
	private final JLabel cardLabel;
	
	private final ActionListener mQuitAction;
	public final GameManager mGameManager;
	
	private final Image gameLogo;
    public Timer timer;
	private int seconds = 15;
	public Boolean restart = false;
	{
		cardButton = new PaintedButton("", ImageLibrary.getImage("images/cards/cardBack_red.png"), ImageLibrary.getImage("images/cards/cardBack_red.png"), 0);
		cardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				mGameManager.requestCardDrawn();
			}
		});
		cardLabel = new JLabel("Cards:");
		cardLabel.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 8));
	}
	
	public GamePanel(ActionListener inQuitAction, GameManager inGameManager, Image inImage){
		gameLogo = inImage;
		mGameManager = inGameManager;
		mQuitAction = inQuitAction;
		setLayout(new GridLayout(boardSize,boardSize));
		tileGrid = new TilePanel[boardSize][boardSize];
		for(int y = 0; y < boardSize; ++y) {
			for(int x = 0; x < boardSize; ++x) {
				if(x == 4 && y == 2) {tileGrid[x][y] = new StartLabelPanel(GameHelpers.getIndexFromColor(Color.YELLOW));}
				else if (x == 2 && y == 7) {tileGrid[x][y] = new HomeLabelPanel(GameHelpers.getIndexFromColor(Color.YELLOW));}
				else if(x == 13 && y == 4) {tileGrid[x][y] = new StartLabelPanel(GameHelpers.getIndexFromColor(Color.GREEN));}
				else if(x == 8 && y == 2) {tileGrid[x][y] = new HomeLabelPanel(GameHelpers.getIndexFromColor(Color.GREEN));}
				else if(x == 11 && y == 13) {tileGrid[x][y] = new StartLabelPanel(GameHelpers.getIndexFromColor(Color.RED));}
				else if(x == 13 && y == 8) {tileGrid[x][y] = new HomeLabelPanel(GameHelpers.getIndexFromColor(Color.RED));}
				else if(x == 2 && y == 11) {tileGrid[x][y] = new StartLabelPanel(GameHelpers.getIndexFromColor(Color.BLUE));}
				else if(x == 7 && y == 13) {tileGrid[x][y] = new HomeLabelPanel(GameHelpers.getIndexFromColor(Color.BLUE));}
				else {tileGrid[x][y] = new TilePanel(mGameManager.getTile(x,y));}
				add(tileGrid[x][y]);
			}
		}

		//TODO: timer
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
				OutMail.send(MessageConstants.BROADCAST, MessageConstants.DECREMENT);

                //restart the timer since the player has moved
                if (restart == true)
                {
                	timer.restart();
                	
                }
                //if the timer has reached 0, end the turn!
                if (seconds ==0)
                {

                    timer.stop();
					OutMail.send(MessageConstants.BROADCAST, MessageConstants.RESTART);

					//mGameManager.endTurn();
                    
                }
               revalidate();
				repaint();
            }
        });
       
		TilePanel cardLabelTile = tileGrid[boardSize/2-1][boardSize/2-1];
		cardLabelTile.setLayout(new GridLayout(1,1));
		cardLabelTile.add(cardLabel);

		TilePanel cardButtonTile = tileGrid[boardSize/2][boardSize/2-1];
		cardButtonTile.setLayout(new GridLayout(1,1));
		cardButtonTile.add(cardButton);
		redraw();
		mGameManager.setGamePanel(this);
	}

	public void updateTiles()
	{
		for(TilePanel row[] : tileGrid) {
			for(TilePanel tp : row) {
				tp.update();
			}
		}
	}
	public void restartTimer()
	{
		restart = true;
	}
	public void decrement() {
		if (seconds ==0)
		{
			//restart the timer, and end the turn!
			mGameManager.endTurn();
			seconds =15;
			revalidate();
			repaint();
			timer.stop();

		}
		else{
			seconds--;
			revalidate();
			repaint();
		}
	}

	public TilePanel getTilePanel(Tile t)
	{
		return tileGrid[t.getX()][t.getY()];
	}
	public void resumeTimer()
	{
		timer.start();
	}

	public void stopTimer()
	{
		timer.stop();
		resetTimer();
	}
	public void resetTimer()
	{
		seconds = 15;
		revalidate();
		repaint();
		timer.stop();
	}
	public void redraw() {
		for(TilePanel row[] : tileGrid) {
			for(TilePanel tp : row) {
				tp.update();
			}
		}
		revalidate();
		repaint();
	}
	
	public void endGame(String winnerName, int score) {
		JOptionPane.showMessageDialog(
				null, 
				mGameManager.getWinner() + " player won!", 
				"Sorry!", 
				JOptionPane.NO_OPTION
			);
		//Quit out if over
		String name = null;
		while(name == null || name.length() < 3 || GameHelpers.containsWhitespace(name)) {
		name = JOptionPane.showInputDialog(
				null,
				"Please enter your name",
				"Score Entry " + score,
				JOptionPane.NO_OPTION);
		if(name.length() < 3) 
			JOptionPane.showMessageDialog(
					null, 
					"Name must be longer than 3 characters and contain no whitespace!", 
					"Sorry!", 
					JOptionPane.ERROR_MESSAGE
				);
		}
		
		try {
			Socket s = new Socket("localhost",8000);
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			pw.println(name + " " + score);
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JButton exit = new JButton("");
		exit.addActionListener(mQuitAction);
		exit.doClick();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(0, 0, getWidth(), getHeight());
		g.drawImage(gameLogo, getWidth() / 3, getHeight() / 4, getWidth() / 3, getHeight() / 6, null);
		g.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.BOLD, 36));
        g.setColor(Color.BLACK);
        //TODO: add the timer
		g.drawString("TIME 0:" + seconds, getWidth()/ 2 - 110, 3*getHeight()/4 - 20 );
	}
	
	public class TilePanel extends PaintedPanel {
		private static final long serialVersionUID = -9071191204545371340L;
		
		private final Tile mTile;
		private final Stack<Component> components;
		
		private PaintedPanel pawn = new PaintedPanel(null);
		private boolean pawnDisplayed = false;
		
		TilePanel(Tile tile) {
			super(null);
			setLayout(new GridBagLayout());
			mTile = tile;
			components = new Stack<Component>();
			if(mTile != null) {
				mDrawBack = true;
				setImage(ImageLibrary.getImage("images/tiles/"+GameHelpers.getNameFromColor(mTile.getColor())+"_tile.png"));
				if(mTile.isStart()) {
					setImage(ImageLibrary.getImage("images/panels/"+GameHelpers.getNameFromColor(mTile.getColor())+"_panel.png"));
					JLabel start = new JLabel("Start");
					start.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 8));
					components.push(start);
				}
				else if(mTile.isHome()) {
					setImage(ImageLibrary.getImage("images/panels/"+GameHelpers.getNameFromColor(mTile.getColor())+"_panel.png"));
					JLabel start = new JLabel("Home");
					start.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 8));
					components.push(start);
				} else if(mTile.doesSlide()) {
					components.push(new PaintedPanel(ImageLibrary.getImage("images/sliders/"+GameHelpers.getNameFromColor(mTile.getColor())+"_slide.png")));
				}
				
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent me) {
						if(SwingUtilities.isRightMouseButton(me)) {
							mGameManager.resetSelection();
						}
						if(!mGameManager.hasCard()) return;
						
						Tile selectedTile = mGameManager.getSelectedTile();
						if(selectedTile == null && !mGameManager.isSorry()) {
							mGameManager.setSelectedTile(mGameManager.getMainPlayer(),mTile,true);
						} else {
							if(mGameManager.isSorry() && mTile.highlighted) mGameManager.requestClicked(mTile,mGameManager.getMainPlayer());
							else if(mTile.highlighted) mGameManager.requestClicked(selectedTile,mGameManager.getMainPlayer());
						}
						redraw();
					}

					@Override
					public void mouseEntered(MouseEvent arg0) {
						if(!mGameManager.hasCard()) return;
						
						if(mGameManager.getSelectedTile() == null) {
							mGameManager.setSelectedTile(mGameManager.getMainPlayer(),mTile,false);
						} else {
							mGameManager.highlightPath(mTile, mGameManager.getMainPlayer().getColor());
						}
						redraw();
					}
					@Override
					public void mouseExited(MouseEvent arg0) {
						if(!mTile.selected)mGameManager.clearHighlight();
						redraw();
					}
				});
			}
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(mTile != null)
			if(mTile.highlighted) {
				g.drawImage(ImageLibrary.getImage("images/panels/"+GameHelpers.getNameFromColor(mGameManager.getMainPlayer().getColor())+"_panel.png"), 0, 0, getWidth(), getHeight(), null);
				Color highlight = mGameManager.getMainPlayer().getColor();
				g.setColor(new Color(highlight.getRed(),highlight.getBlue(),highlight.getGreen(),63));//highlight
		        g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
		
		protected void update() {
			if(mTile == null) return;
			if(mTile.isOccupied() && !pawnDisplayed) {
				pawnDisplayed = true;
				pawn.setImage(ImageLibrary.getImage("images/pawns/"+GameHelpers.getNameFromColor(mTile.getPawnColor())+"_pawn.png"));
				components.push(pawn);
			}
			if(mTile.isOccupied() && pawnDisplayed) {
				pawn.setImage(ImageLibrary.getImage("images/pawns/"+GameHelpers.getNameFromColor(mTile.getPawnColor())+"_pawn.png"));
			}
			if(!mTile.isOccupied() && pawnDisplayed) {
				pawnDisplayed = false;
				components.pop();
			}
			removeAll();
			if(!components.isEmpty())add(components.peek());
		}

		public void updateGUI()
		{
			redraw();
		}
	}
	
	//Used for the start counter display
		class StartLabelPanel extends TilePanel{
			private static final long serialVersionUID = -9166979703140637366L;
			private final int mPlayerNum;
			JLabel mLabel;
			{
				mLabel = new JLabel();
				mLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 12));
				add(mLabel);
			}
			StartLabelPanel(int numPlayer) {
				super(null);
				mPlayerNum = numPlayer;
			}
			@Override
			protected void update() {
				mLabel.setText(mGameManager.getPlayerStartCount(mPlayerNum));
			}
		}
		
		//Used for the home counter display
		class HomeLabelPanel extends TilePanel{
			private static final long serialVersionUID = -9166979703540637366L;
			private final int mPlayerNum;
			JLabel mLabel;
			{
				mLabel = new JLabel();
				mLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 12));
				add(mLabel);
			}
			HomeLabelPanel(int numPlayer) {
				super(null);
				mPlayerNum = numPlayer;
			}
			@Override
			protected void update() {
				mLabel.setText(mGameManager.getPlayerHomeCount(mPlayerNum));
			}
		}
}
