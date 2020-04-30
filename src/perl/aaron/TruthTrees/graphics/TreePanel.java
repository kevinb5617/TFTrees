package perl.aaron.TruthTrees.graphics;

import static perl.aaron.TruthTrees.Branch.MIN_WIDTH;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import perl.aaron.TruthTrees.Branch;
import perl.aaron.TruthTrees.BranchLine;
import perl.aaron.TruthTrees.BranchTerminator;
import perl.aaron.TruthTrees.ExpressionParser;
import perl.aaron.TruthTrees.logic.Statement;
import perl.aaron.TruthTrees.util.UserError;

class Global {
	public static String var;
	public static Statement s1;
	public static Statement s2;
}

class RadioPanel extends JFrame {

	private static final long serialVersionUID = 1L;

	JRadioButton jRadioButton1;

	JRadioButton jRadioButton2;

	JButton jButton;

	ButtonGroup buttonGroup;

	JLabel label;

	ArrayList<JRadioButton> buttons;

	// Constructor of Demo class.
	public RadioPanel(Set<String> variables) {
		// Setting layout as null of JFrame.
		this.setLayout(null);

		jButton = new JButton("Submit"); // Initialization of object of "ButtonGroup" class.
		buttonGroup = new ButtonGroup();
		label = new JLabel("Choose variable to split");

		int x = 200;
		int y = 200;
		buttons = new ArrayList<>();

		Iterator itr = variables.iterator();
		while (itr.hasNext()) {
			JRadioButton button = new JRadioButton();
			button.setText(itr.next().toString());
			buttonGroup.add(button);
			buttons.add(button);
			this.add(button);
			button.setBounds(x, 30, y, 50);
			x += 100;
			y -= 40;
		}

		// Setting Bounds of "jButton".
		jButton.setBounds(125, 90, 80, 30);

		// Setting Bounds of JLabel "L2".
		label.setBounds(20, 30, 200, 50);

		this.add(jButton);

		// Adding JLabel "L2" on JFrame.
		this.add(label);

		// Adding Listener to JButton.
		jButton.addActionListener(new ActionListener() {
			// Anonymous class.

			public void actionPerformed(ActionEvent e) {
				// Override Method

				// Declaration of String class Objects.
				String qual = " ";
				for (int i = 0; i < buttons.size(); i++) {
					JRadioButton button = buttons.get(i);
					if (button.isSelected()) {
						qual = button.getText();
					}
				}
				if (qual.equals(" ")) {

					qual = "NO variable selected";
				}

				// MessageDialog to show information selected radion buttons.
				JOptionPane.showMessageDialog(RadioPanel.this, qual);
				Global.var = new String(qual);
				RadioPanel.this.dispose();
			}
		});
	}
}

/**
 * An extension of JPanel for displaying and interacting with a sequence of
 * TreeLines
 * 
 * @author Aaron Perl, Sarah Mogielnicki
 *
 */
public class TreePanel extends JPanel {

	private static final long serialVersionUID = 2267768929169530856L;
	private static final int UNDO_STACK_SIZE = 32;
	private static final int REDO_STACK_SIZE = 32;
	public static String var;

	private Point center = new Point(0, -50);
	private Point prevCenter = null; //TODO: remove global
	private Point clickPoint = null; //TODO: remove global
	private float size = 12;
	private BranchLine editLine = null;
	private final Map<Branch, JButton> addBranchMap = new HashMap<>();
	private final Map<Branch, JButton> addLineMap = new HashMap<>();
	private final Map<Branch, JButton> branchMap = new HashMap<>();
	private final Map<Branch, JButton> terminateMap = new HashMap<>();
	private final Map<JTextField, BranchLine> lineMap = new HashMap<>();
	private final Map<BranchLine, JTextField> reverseLineMap = new HashMap<>();
	private Set<BranchLine> selectedLines = null; //TODO: remove global
	private Set<Branch> selectedBranches = null; //TODO: remove global
	private Branch premises = addBranch(null, true);
	private final Deque<Branch> undoStack = new ArrayDeque<>(UNDO_STACK_SIZE);
	private final Deque<Branch> redoStack = new ArrayDeque<>(REDO_STACK_SIZE);
	private int zoomLevel = 0;
	private int decompNumber = 1;
	
	private Branch root = addBranch(premises, false);

	private double zoomMultiplicationFactor = 1.1;
	
	private enum Completion {
		ALL_CLOSED,
		ONE_OPEN,
		INVALID
	}

	public TreePanel() {
		this(true);
	}
	
	private void resetVars() {
		clickPoint = null;
		prevCenter = null;
		
		center = new Point(0, -50);
		size = 12f;
		zoomLevel = 0;
		decompNumber = 1;
		editLine = null;
		selectedLines = null;
		selectedBranches = null;
		addBranchMap.clear();
		addLineMap.clear();
		branchMap.clear();
		terminateMap.clear();
		lineMap.clear();
		reverseLineMap.clear();
		this.setFont(this.getFont().deriveFont(size));
		premises = addBranch(null, true);
		undoStack.clear();
		redoStack.clear();

		root = addBranch(premises, false);
	}

	public TreePanel(boolean addFirstLine) {
		super();
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 0));
		setLayout(null);
		
		setFocusable(true);
		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				prevCenter = center;
				clickPoint = e.getPoint();
				requestFocus();
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				center = new Point(e.getPoint().x - clickPoint.x + prevCenter.x,
						e.getPoint().y - clickPoint.y + prevCenter.y);
				moveComponents();
				validate();
				repaint();
			}
		});

	}

	private void recordState() {
		if (premises != null) {
			Branch treeCopy = premises.deepCopy();

			Map<BranchLine, BranchLine> lineMap = new HashMap<BranchLine, BranchLine>();
			Map<Branch, Branch> branchMap = new HashMap<Branch, Branch>();

			mapNewToOld(premises, treeCopy, lineMap, branchMap);
			addLineReferences(lineMap, branchMap);

			undoStack.push(treeCopy);
			redoStack.clear();
		}
	}

	/**
	 * Maps every item in a copy of a branch to the corresponding item in the
	 * original
	 * 
	 * @param oldBranch The original branch
	 * @param newBranch The copy of the original branch
	 * @param lineMap   A map of old lines to new lines to be populated
	 * @param branchMap A map of old branches to new branches to be populated
	 */
	private void mapNewToOld(Branch oldBranch, Branch newBranch, Map<BranchLine, BranchLine> lineMap,
			Map<Branch, Branch> branchMap) {
		branchMap.put(oldBranch, newBranch);
		for (int i = 0; i < oldBranch.numLines(); i++) {
			lineMap.put(oldBranch.getLine(i), newBranch.getLine(i));
		}
		Iterator<Branch> oldIter = oldBranch.getBranches().iterator();
		Iterator<Branch> newIter = newBranch.getBranches().iterator();
		while (oldIter.hasNext()) {
			mapNewToOld(oldIter.next(), newIter.next(), lineMap, branchMap);
		}
	}

	/**
	 * Adds all references (decompositions, etc.) from lines in a tree to a copy of
	 * that tree, updating them to point to the components in the copy
	 * 
	 * @param lineMap   The map of old lines to each corresponding lines in the copy
	 * @param branchMap The map of branches to each corresponding branch in the copy
	 */
	private void addLineReferences(Map<BranchLine, BranchLine> lineMap, Map<Branch, Branch> branchMap) {
		for (BranchLine oldLine : lineMap.keySet()) {
			BranchLine newLine = lineMap.get(oldLine);

			BranchLine oldDecomposedFrom = oldLine.getDecomposedFrom();
			BranchLine newDecomposedFrom = lineMap.get(oldDecomposedFrom);
			newLine.setDecomposedFrom(newDecomposedFrom);

			Set<BranchLine> oldSelectedLineSet = oldLine.getSelectedLines();
			Set<BranchLine> newSelectedLineSet = newLine.getSelectedLines();
			for (BranchLine oldSelected : oldSelectedLineSet) {
				BranchLine newSelected = lineMap.get(oldSelected);
				newSelectedLineSet.add(newSelected);
			}

			Set<Branch> oldSelectedBranchSet = oldLine.getSelectedBranches();
			Set<Branch> newSelectedBranchSet = newLine.getSelectedBranches();
			for (Branch oldSelected : oldSelectedBranchSet) {
				Branch newSelected = branchMap.get(oldSelected);
				newSelectedBranchSet.add(newSelected);
			}

			newLine.setIsPremise(oldLine.isPremise());
		}
	}

	/**
	 * Undoes the previous state change.
	 */
	public void undoState() {
		if (!undoStack.isEmpty()) {
			redoStack.push(premises.deepCopy());
			premises = undoStack.pop();
			root = premises.getBranches().iterator().next();
			editLine = null;
			resetAllComponents();
			moveComponents();
			repaint();
		}
	}

	/**
	 * Performs the previously undone state change again
	 */
	public void redoState() {
		if (!redoStack.isEmpty()) {
			undoStack.push(premises.deepCopy());
			premises = redoStack.pop();
			root = premises.getBranches().iterator().next();
			editLine = null;
			resetAllComponents();
			moveComponents();
			repaint();
		}
	}
	
	public void clear() {
		resetVars();
	}

	/**
	 * Deletes all components saved and recreates them for the current tree.
	 */
	private void resetAllComponents() {
		addBranchMap.clear();
		addLineMap.clear();
		branchMap.clear();
		terminateMap.clear();
		lineMap.clear();
		reverseLineMap.clear();

		addComponentsRecursively(premises);
	}

	/**
	 * Creates all associated component for a given branch and its ancestors
	 * 
	 * @param b The branch to recursively create components for
	 */
	private void addComponentsRecursively(Branch b) {
		makeButtonsForBranch(b);
		for (int i = 0; i < b.numLines(); i++) {
			BranchLine line = b.getLine(i);
			makeTextFieldForLine(line, b, line instanceof BranchTerminator);
		}
		for (Branch child : b.getBranches()) {
			addComponentsRecursively(child);
		}
	}

	/**
	 * Makes a new BranchLine that is a premise, and sets its statement to s
	 * 
	 * @param s The premise that is added
	 */
	public void addPremise(Statement s) {
		recordState();
		BranchLine newLine = addLine(premises);
		newLine.setIsPremise(true);
		if (s != null) {
			newLine.setStatement(s);
			reverseLineMap.get(newLine).setText(s.toString());
			moveComponents();
		}
	}

	/**
	 * Makes a new BranchLine that is a premise, with no statement
	 */
	public void addPremise() {
		addPremise(null);
	}

	/**
	 * Checks that the line is decomposed properly
	 * 
	 * @param l The BranchLine that is being checked
	 * @return null if decomposed properly, an error message otherwise
	 */
	private String checkLine(BranchLine l) {
		return l.verifyDecomposition();
	}

	/**
	 * Checks all the lines in a branch to see if they are decomposed properly
	 * 
	 * @param b Branch being checked
	 * @return null if decomposed properly, an error message otherwise
	 */
	private void checkBranch(Branch b) throws UserError { //TODO: change returns to throws
		for (int i = 0; i < b.numLines(); i++) {
			BranchLine curLine = b.getLine(i);
			String ret = checkLine(curLine);
			if (ret != null)
				return ret;
		}
		for (Branch curBranch : b.getBranches()) {
			String ret = checkBranch(curBranch);
			if (ret != null)
				return ret;
		}
		return null;
	}

	/**
	 * Checks that the tree is complete (by checking that no branches are open and
	 * that all of the branches are closed).
	 * 
	 * @return 0 if all branches close with no open branches, 1 if all branches
	 *         terminate but at least one is marked open, -1 otherwise
	 */
	public Completion checkCompletion() {
		boolean isOpen = checkForOpenBranch(root);
		boolean allClosed = checkForAllClosed(root);
		if (!isOpen && allClosed)
			return Completion.ALL_CLOSED;
		else if (!allClosed && !isOpen)
			return Completion.INVALID;
		else
			return Completion.ONE_OPEN;
	}

	/**
	 * Recursively checks if b or any of its children are open
	 * 
	 * @param b the Branch that is being checked
	 * @return true if b or any of its children are open
	 */
	public static boolean checkForOpenBranch(Branch b) {
		if (b.isOpen())
			return true;
		for (Branch child : b.getBranches()) {
			if (checkForOpenBranch(child))
				return true;
		}
		return false;
	}

	/**
	 * Recursively checks if b and all if its children are closed
	 * 
	 * @param b the Branch being checked for closed
	 * @return false if b or one of its children is not closed, true otherwise
	 */
	private boolean checkForAllClosed(Branch b) {
		if (b.getBranches().size() == 0 && !b.isClosed())
			return false;
		for (Branch child : b.getBranches()) {
			if (!checkForAllClosed(child))
				return false;
		}
		return true;
	}

	/**
	 * Recursively checks if the branch b has a valid open terminator
	 * 
	 * @param b the branch being checked for a valid open branch
	 * @return true if b has a valid open terminator, false otherwise
	 */
	private void verifyOpenTerminator(Branch b) throws UserError {
		// If there are no open branches anywhere return false

		if (b.getBranches().size() == 0 && b.isOpen()) {
			return b.verifyTerminations();
		}

		if (b.getBranches().size() == 0 && !b.isOpen())
			return false;

		for (Branch child : b.getBranches()) {
			if (checkForOpenBranch(child)) {
				return verifyOpenTerminator(child);
			}
		}

		return false;

	}

	/**
	 * Recursively checks if b and all if its children are have verified Terminators
	 * 
	 * @param b the Branch being checked for verified terminators
	 * @return false if b or one of its children has a terminator that is not
	 *         verified, true otherwise
	 */
	private void verifyTerminators(Branch b) throws UserError { //TODO: change returns to throws

		if (checkForOpenBranch(b)) { // If there are open branches in the current level or below

			// Check that the open branch is valid
			verifyOpenTerminator(b);
			
		} else { // No open branches in tree, proceed as normal
			if (b.getBranches().size() == 0 && !b.verifyTerminations()) {
				return false;
			}

			for (Branch child : b.getBranches()) {
				if (!verifyTerminators(child)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Walks through the tree and returns a set of the bottom branches that contain
	 * an open terminator
	 * 
	 * @param b    Branch to start searching at
	 * @param open Empty set of Branches (for recursion)
	 * @return The set of all branches with an open terminator
	 */
	private Set<Branch> findOpenBranches(Branch b, Set<Branch> open) {
		boolean hasOpen = checkForOpenBranch(b);
		if (hasOpen) {

			if (b.getBranches().size() > 0) {
				// Has children
				for (Branch child : b.getBranches()) {

					if (checkForOpenBranch(child)) { // Recurse into children with open branches
						return findOpenBranches(child, open);
					}
				}
			} else {
				// No children
				open.add(b);
				return open;
			}
		} else {
			// No open branch
			open.add(b);
			return open;
		}
		return open;
	}
	
	private void openVerifyLines(Branch b) throws UserError {
		openVerifyLines(b, false);
	}

	// 'lax' indicates that lines need not be decomposed, but still check for validity if they are
	private void openVerifyLines(Branch b, boolean lax) throws UserError { //TODO throw instead of return
		
		String error;
		for(BranchLine line: b.getLines())
			if((error = line.verifyDecompositionOpen(lax)) != null)
				return error;
		for(Branch child: b.getBranches()) {
			boolean doLax = lax || !checkForOpenBranch(child);
			if((error = openVerifyLines(child, doLax)) != null)
				return error;
		}
		return null;
		
//		return error;
	}

	/**
	 * Checks for any unexpected constants in a branch
	 * 
	 * @param b Branch to be checked
	 * @return Empty string if ok, error message if problem
	 */
	public String checkBranchConstants(Branch b) {

		String ret = "";

		if (b.numLines() > 0) {

			for (int i = 0; i < b.numLines(); ++i) {
				BranchLine line = b.getLine(i);

				String vRet = line.verifyDecompositionOpen();
				if (vRet != null) {
					ret += vRet;
				}
			}
		}

		for (Branch child : b.getBranches()) {
			ret += checkBranchConstants(child);
		}
		return ret;
	}
	
	//TODO

	/**
	 * Runs all of the "check" methods on the whole tree.
	 * 
	 * @return null if tree is correct and complete, error message otherwise
	 */
	public void check() throws UserError {
		verifyTerminators(root);

		switch (checkCompletion()) {
		case ALL_CLOSED:
			checkBranch(premises);
			checkBranch(root);
			return;
		case ONE_OPEN:
			openVerifyLines(premises);
			openVerifyLines(root);
			return;
		default: // INVALID
			throw new UserError("Not all branches are closed and no branch has been marked as open!");
		}
	}

	/**
	 * Checks the selected line.
	 * 
	 * @return null if the line is fine, and an error message otherwise
	 */
	public void checkSelectedLine() throws UserError {
		if (editLine != null) //TODO: change editline to Option, throw UserError
			return checkLine(editLine);
		return "No statement is currently selected!";
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public Branch addBranch(Branch parent) {
		return addBranch(parent, true);
	}

	/**
	 * Makes a new Branch and adds it to the parent branch. Adds the first line to
	 * the branch if addFirstLine is true.
	 * 
	 * @param parent       the root branch for this branch
	 * @param addFirstLine if true, the method will add a line to the new branch
	 * @return the new branch that was added
	 */
	public Branch addBranch(Branch parent, boolean addFirstLine) {
		recordState();
		Branch newBranch = new Branch(parent);
		newBranch.setFontMetrics(getFontMetrics(getFont()));
		makeButtonsForBranch(newBranch);
		if (parent != null)
			parent.addBranch(newBranch);
		if (addFirstLine) {
			addLine(newBranch);
			if (parent == null)
				newBranch.getLine(0).setIsPremise(true);
		}
		moveComponents();
		repaint();
		return newBranch;
	}

	public Branch addBranch(Branch parent, boolean addFirstLine, boolean wasNotTyped, Statement s) {
		recordState();
		Branch newBranch = new Branch(parent);
		newBranch.setFontMetrics(getFontMetrics(getFont()));
		makeButtonsForBranch(newBranch);
		if (parent != null)
			parent.addBranch(newBranch);
		if (addFirstLine) {
			addLine(newBranch, s);
			newBranch.getLine(0).setIsPremise(true);
			if (parent == null)
				newBranch.getLine(0).setIsPremise(true);
		}
		moveComponents();
		repaint();
		return newBranch;
	}

	/**
	 * Makes all the buttons for the branch
	 * 
	 * @param b Branch that will get the buttons
	 */
	private void makeButtonsForBranch(Branch b) {
		final Branch myBranch = b;
		JButton branchButton = new JButton("Add Branch");
		JButton lineButton = new JButton("Add Line");
		JButton terminateButton = new JButton("Terminate");
		JButton decompButton = new JButton();
		decompButton.setOpaque(false);
		decompButton.setContentAreaFilled(false);
		decompButton.setBorderPainted(false);
		decompButton.setFocusable(false);
		branchButton.setMargin(new Insets(1, 1, 1, 1));
		lineButton.setMargin(new Insets(1, 1, 1, 1));
		terminateButton.setMargin(new Insets(1, 1, 1, 1));
		branchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addBranch(myBranch);
			}
		});
		lineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addLine(myBranch);
			}
		});
		terminateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTerminator(myBranch);
			}
		});
		decompButton.addMouseListener(new MouseListener() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (selectedBranches != null && (SwingUtilities.isRightMouseButton(e) || e.isControlDown())) {
					if (selectedBranches.contains(myBranch)) {
						selectedBranches.remove(myBranch);
						myBranch.setDecomposedFrom(null);
					} else if (myBranch.getDecomposedFrom() == null) {
						selectedBranches.add(myBranch);
						myBranch.setDecomposedFrom(editLine);
					}
					TreePanel.this.repaint();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		add(branchButton);
		add(lineButton);
		add(terminateButton);
		add(decompButton);
		addBranchMap.put(b, branchButton);
		addLineMap.put(b, lineButton);
		branchMap.put(b, decompButton);
		terminateMap.put(b, terminateButton);
	}

	/**
	 * Checks if Branch b is selected.
	 * 
	 * @param b the Branch that is being checked for selection
	 * @return true if b is selected, false otherwise
	 */
	private boolean isSelected(BranchLine b) {
		if (selectedLines != null)
			return selectedLines.contains(b);
		return false;
	}

	private void toggleSelected(BranchLine b, Set<BranchLine> curSelected) {
		if (curSelected.contains(b)) {
			curSelected.remove(b);
			reverseLineMap.get(b).setBackground(BranchLine.DEFAULT_COLOR);
			if (!(editLine instanceof BranchTerminator))
				b.setDecomposedFrom(null);
		} else {
			curSelected.add(b);
			reverseLineMap.get(b).setBackground(BranchLine.SELECTED_COLOR);
			if (!(editLine instanceof BranchTerminator))
				b.setDecomposedFrom(editLine);
		}
	}

	private void toggleSelected(BranchLine b) {
		toggleSelected(b, selectedLines);
	}

	private void moveBranch(Branch b, Point origin) {
		int verticalOffset = 0;
		int maxLineWidth = b.getWidestLine();
		int maxWidth = b.getWidestChild();
		for (int i = 0; i < b.numLines(); i++) {
			BranchLine curLine = b.getLine(i);
			JTextField curField = reverseLineMap.get(curLine);
			if (isSelected(curLine))
				curField.setBackground(BranchLine.SELECTED_COLOR);
			else if (curLine == editLine)
				curField.setBackground(BranchLine.EDIT_COLOR);
			else
				curField.setBackground(BranchLine.DEFAULT_COLOR);
			curField.setBounds(origin.x - maxLineWidth / 2, origin.y + verticalOffset, maxLineWidth, b.getLineHeight());
			curField.repaint();
			verticalOffset += b.getLineHeight();
			if (curLine.decompNum != -1){

				String tickMark = "\u221A" + generateSubscript(curLine.decompNum);
				Graphics2D g2d = (Graphics2D)this.getGraphics();
				Point p = curField.getLocation();
				p.setLocation((p.getX()+curField.getWidth()+10), (p.getY()+(curField.getHeight()/2)+7));
				drawStringAt(g2d, p, tickMark);
			}
		}
		if (b != premises) {
			JButton lineButton = addLineMap.get(b);
			JButton addButton = addBranchMap.get(b);
			JButton branchButton = branchMap.get(b);
			JButton terminateButton = terminateMap.get(b);
			int horizontalOffset = (maxWidth + Branch.BRANCH_SEPARATION) * (b.getBranches().size() - 1);
			horizontalOffset /= -2;
			if (!b.isClosed() && !b.isOpen()) {
				lineButton.setBounds(origin.x - maxLineWidth / 2, origin.y + verticalOffset, maxLineWidth,
						b.getLineHeight());
				verticalOffset += b.getLineHeight();
				addButton.setBounds(origin.x - maxLineWidth / 2, origin.y + verticalOffset, maxLineWidth,
						b.getLineHeight());
				verticalOffset += b.getLineHeight();
				if (b.getBranches().size() == 0) {
					terminateButton.setBounds(origin.x - maxLineWidth / 2, origin.y + verticalOffset, maxLineWidth,
							b.getLineHeight());
					terminateButton.setVisible(true);
					terminateButton.setEnabled(true);
					verticalOffset += b.getLineHeight();
				} else {
					terminateButton.setVisible(false);
					terminateButton.setEnabled(false);
				}
				terminateButton.repaint();
				branchButton.setBounds(origin.x + horizontalOffset - maxWidth / 2, origin.y + verticalOffset,
						-horizontalOffset * 2 + maxWidth, Branch.VERTICAL_GAP);
				addButton.setVisible(true);
				addButton.setEnabled(true);
				lineButton.setVisible(true);
				lineButton.setEnabled(true);
				branchButton.setEnabled(true);
			} else {
				addButton.setVisible(false);
				addButton.setEnabled(false);
				lineButton.setVisible(false);
				lineButton.setEnabled(false);
				branchButton.setEnabled(false);
				terminateButton.setVisible(false);
				terminateButton.setEnabled(false);
			}
			addButton.repaint();
			lineButton.repaint();
			branchButton.repaint();
			terminateButton.repaint();
			verticalOffset += Branch.VERTICAL_GAP;
			for (Branch curChild : b.getBranches()) {
				moveBranch(curChild, new Point(origin.x + horizontalOffset, origin.y + verticalOffset));
				horizontalOffset += (maxWidth + Branch.BRANCH_SEPARATION);
			}
		}
	}

	public void moveComponents() {
		Point origin = new Point(center.x + getWidth() / 2, center.y + getHeight() / 2);
		if (premises != null) {
			moveBranch(premises, origin);
			origin.translate(0, premises.getLineHeight() * premises.numLines());
		}
		origin.translate(0, 20);
		if (root != null) {
			moveBranch(root, origin);
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(800, 600);
	}

	/**
	 * Adds a BranchLine that is not a terminator
	 * 
	 * @param b the Branch that the BranchLine is added to
	 * @return the BranchLine that was added
	 */
	private BranchLine addLine(final Branch b) {
		return addLine(b, false);
	}

	/**
	 * Adds a BranchLine that is not a terminator
	 * Specific to the split function where user does not add the line
	 * 
	 * @param b the Branch that the BranchLine is added to
	 * @param s the Statement that is added to the Branchline
	 * 
	 * @return the BranchLine that was added
	 */
	private BranchLine addLine(final Branch b, final Statement s) {
		return addLine(b, false, true, true, s);
	}

	/**
	 * Adds a BranchLine that may or may not be a terminator
	 * 
	 * @param b            the Branch that the BranchLine is added to
	 * @param isTerminator is true if if this BranchLine is a terminator
	 * @return the BranchLine that was added
	 */
	private BranchLine addLine(final Branch b, final boolean isTerminator) {
		return addLine(b, isTerminator, true);
	}

	/**
	 * Adds a new branch line, which can be a terminator.
	 * 
	 * @param b            the Branch to which the line is being added
	 * @param isTerminator true if the line is a terminator
	 * @param isClose
	 * @return the BranchLine that was added
	 */
	private BranchLine addLine(final Branch b, final boolean isTerminator, final boolean isClose) {
		recordState();
		final BranchLine newLine;
		if (isTerminator) {
			newLine = new BranchTerminator(b);
			if (!isClose)
				((BranchTerminator) newLine).switchIsClose();
			b.addTerminator((BranchTerminator) newLine);
		} else
			newLine = b.addStatement(null);
		makeTextFieldForLine(newLine, b, isTerminator);
		moveComponents();
		return newLine;
	}

	/**
	 * Adds a new branch line, which can be a terminator.
	 * Used when user does not add the line
	 * 
	 * @param b            the Branch to which the line is being added
	 * @param isTerminator true if the line is a terminator
	 * @param isClose
	 * @param wasNotTyped check if user added the line or code did
	 * @param s the Statement added to the Branchline
	 * 
	 * @return the BranchLine that was added
	 */
	private BranchLine addLine(final Branch b, final boolean isTerminator, final boolean isClose,
			final boolean wasNotTyped, final Statement s) {
		recordState();
		final BranchLine newLine;
		if (wasNotTyped) {
			newLine = b.addStatement(s);
		} else if (isTerminator) {
			newLine = new BranchTerminator(b);
			if (!isClose)
				((BranchTerminator) newLine).switchIsClose();
			b.addTerminator((BranchTerminator) newLine);
		} else
			newLine = b.addStatement(null);
		if (newLine.getStatement() != null)
			System.out.println("newline:" + newLine.getStatement().toString());
		makeTextFieldForLine(newLine, b, isTerminator);
		moveComponents();
		return newLine;
	}

	public void addLineBefore() {
		if (editLine != null) {
			System.out.println("Adding line before " + editLine);
			for (int i = 0; i < editLine.getParent().numLines(); i++) {
				if (editLine.getParent().getLine(i) == editLine) {
					final BranchLine newline;
					newline = editLine.getParent().addStatement(null, i);
					makeTextFieldForLine(newline, editLine.getParent(), false);
					moveComponents();
					return;
				}
			}
		}
	}

	public void addLineAfter() {
		if (editLine != null && !(editLine instanceof BranchTerminator)) {
			System.out.println("Adding line after " + editLine);
			for (int i = 0; i < editLine.getParent().numLines(); i++) {
				if (editLine.getParent().getLine(i) == editLine) {
					final BranchLine newline;
					newline = editLine.getParent().addStatement(null, i + 1);
					makeTextFieldForLine(newline, editLine.getParent(), false);
					moveComponents();
					return;
				}
			}
		}
	}

	private void makeTextFieldForLine(final BranchLine line, final Branch b, final boolean isTerminator) {
		final JTextField newField = new JTextField("");
		newField.setUI(new BasicTextFieldUI() {
			@Override
			public void paintBackground(Graphics g) {
				super.paintBackground(g);
				Color old = g.getColor();
				g.setColor(getBackground());
				g.fillRect(getX(), getY(), getWidth(), getHeight());
				g.setColor(old);
			}
		});
		if (line.getStatement() != null) {
			System.out.println("toString:" + line.getStatement().toString());
			newField.setText(line.getStatement().toString());
		}
		if (isTerminator) {
			newField.setText(line.toString());
			newField.setForeground(new Color(0.7f, 0.0f, 0.0f));
		}
		if (b == premises)
			line.setIsPremise(true);
		newField.setEditable(false);
		newField.setFocusable(false);
		newField.setHorizontalAlignment(JTextField.CENTER);
		newField.setFont(this.getFont().deriveFont(size));
		((AbstractDocument) newField.getDocument()).setDocumentFilter(new DocumentFilter() {
			public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException {
				if (string.equals("$"))
					super.insertString(fb, offset, "\u2192", attr);
				else if (string.equals("%"))
					super.insertString(fb, offset, "\u2194", attr);
				else if (string.equals("@"))
					super.insertString(fb, offset, "\u2200", attr);
				else if (string.equals("/"))
					super.insertString(fb, offset, "\u2203", attr);
				else if (string.equals("|"))
					super.insertString(fb, offset, "\u2228", attr);
				else if (string.equals("&"))
					super.insertString(fb, offset, "\u2227", attr);
				else if (string.equals("~"))
					super.insertString(fb, offset, "\u00AC", attr);
				else if (string.equals("!"))
					super.insertString(fb, offset, "\u00AC", attr);
				else
					super.insertString(fb, offset, string, attr);
			}

			public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
				super.remove(fb, offset, length);
			}

			public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				if (text.equals("$"))
					super.replace(fb, offset, length, "\u2192", attrs);
				else if (text.equals("%"))
					super.replace(fb, offset, length, "\u2194", attrs);
				else if (text.equals("@"))
					super.replace(fb, offset, length, "\u2200", attrs);
				else if (text.equals("/"))
					super.replace(fb, offset, length, "\u2203", attrs);
				else if (text.equals("|"))
					super.replace(fb, offset, length, "\u2228", attrs);
				else if (text.equals("&"))
					super.replace(fb, offset, length, "\u2227", attrs);
				else if (text.equals("~"))
					super.replace(fb, offset, length, "\u00AC", attrs);
				else if (text.equals("!"))
					super.replace(fb, offset, length, "\u00AC", attrs);
				else
					super.replace(fb, offset, length, text, attrs);

			}
		});
		newField.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown()) {
					if (editLine != null) {
						reverseLineMap.get(editLine).setEditable(false);
						reverseLineMap.get(editLine).setFocusable(false);
					}
					if (!isTerminator) {
						newField.setEditable(true);
						newField.setFocusable(true);
					}
					newField.requestFocus();
					editLine = lineMap.get(newField);
					selectedLines = lineMap.get(newField).getSelectedLines();
					selectedBranches = lineMap.get(newField).getSelectedBranches();
					moveComponents();
					repaint();
				} else if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
					BranchLine curLine = lineMap.get(newField);
					if (!isTerminator) {
						if (editLine != curLine && editLine != null && (editLine == curLine.getDecomposedFrom()
								|| curLine.getDecomposedFrom() == null || editLine instanceof BranchTerminator))
							toggleSelected(curLine);
					} else {
						((BranchTerminator) lineMap.get(newField)).switchIsClose();
						newField.setText(lineMap.get(newField).toString());
					}
				}

			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		newField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				TreePanel.this.dispatchEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				TreePanel.this.dispatchEvent(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				line.typing = true;
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					line.typing = false;
					TreePanel.this.requestFocus();
				}
				TreePanel.this.dispatchEvent(e);
				line.currentTyping = newField.getText();
				moveComponents();
			}
		});
		// Parse the statement when focus is lost
		newField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				line.typing = false;
				Statement newStatement = ExpressionParser.parseExpression(newField.getText());
				if (newStatement != null) {
					if (newField.getParent() != null) // Ensures that the state isn't recorded twice when deleting a
														// branch
						recordState();
					line.setStatement(newStatement);
					b.calculateWidestLine();
					newField.setText(newStatement.toString());
				} else {
					if (!newField.getText().equals("")) {
						if (line.getStatement() != null)
							newField.setText(line.toString());
						else
							newField.setText("");
						JOptionPane.showMessageDialog(null, "Error: Invalid logical statement", "Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						if (newField.getParent() != null) // Ensures that the state isn't recorded twice when deleting a
															// branch
							recordState();
						line.setStatement(null);
					}
				}
				moveComponents();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		lineMap.put(newField, line);
		reverseLineMap.put(line, newField);
		add(newField);
		newField.setEditable(false);
	}

	/**
	 * Adds a statement to a Branch
	 * 
	 * @param b the Branch to which the Statement is added
	 * @param s the Statement added to the Branch
	 * @return the BranchLine (which contains Statement s) added to Branch b
	 */
	public BranchLine addStatement(Branch b, Statement s) {
		BranchLine newLine = addLine(b);
		newLine.setStatement(s);
		reverseLineMap.get(newLine).setText(s.toString());
		moveComponents();
		return newLine;
	}

	// temporary
	/**
	 * Adds a statement to the root of the tree
	 * 
	 * @param s the Statement added to the tree
	 */
	public void addStatement(Statement s) {
		addStatement(root, s);
	}

	/**
	 * Adds a closed BranchTerminator to a branch
	 * 
	 * @param b the Branch to which the BranchTerminator is added
	 * @return the BranchTerminator line that was added
	 */
	public BranchTerminator addTerminator(Branch b) {
		return (BranchTerminator) addLine(b, true, true);
	}

	/**
	 * Adds an open BranchTerminator to a branch
	 * 
	 * @param b the Branch to which the BranchTerminator is added
	 * @return the BranchTerminator line that was added
	 */
	public BranchTerminator addOpenTerminator(Branch b) {
		return (BranchTerminator) addLine(b, true, false);
	}

	public void drawBranching(Branch b, Graphics2D g) {
		if (selectedBranches != null && selectedBranches.contains(b))
			g.setColor(BranchLine.SELECTED_COLOR);
		else
			g.setColor(BranchLine.DEFAULT_COLOR);
		JButton addButton = addBranchMap.get(b);
		if (addButton != null) {
			int midX = addButton.getX() + addButton.getWidth() / 2;
			int topY = (int) addButton.getBounds().getMaxY();
			if (b.getBranches().size() > 1) {
				int midY = topY + Branch.VERTICAL_GAP / 2;
				int bottomY = topY + Branch.VERTICAL_GAP;
				int leftX = (b.getWidestChild() + Branch.BRANCH_SEPARATION) * (b.getBranches().size() - 1);
				leftX /= 2;
				leftX = midX - leftX;
				int rightX = leftX + (b.getWidestChild() + Branch.BRANCH_SEPARATION) * (b.getBranches().size() - 1);
				g.drawLine(midX, topY, midX, midY);
				g.drawLine(leftX, midY, rightX, midY);
				int curX = leftX;
				for (Branch curBranch : b.getBranches()) {
					g.drawLine(curX, midY, curX, bottomY);
					curX += (b.getWidestChild() + Branch.BRANCH_SEPARATION);
					if (curBranch.getBranches().size() > 0)
						drawBranching(curBranch, g);
				}
			} else
				g.drawLine(midX, topY, midX, topY + Branch.VERTICAL_GAP);
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setClip(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(1.0f, 1.0f, 1.0f));
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(new Color(0.0f, 0.0f, 0.0f));
		g2d.setStroke(new BasicStroke(4.0f));
		drawStringAt(g2d, new Point(center.x + getWidth() / 2, center.y + getHeight() / 2), "Premises");

		drawStringAt(g2d, new Point(center.x + getWidth() / 2,
				center.y + getHeight() / 2 + premises.numLines() * premises.getLineHeight() + Branch.VERTICAL_GAP),
				"Decomposition");

		for (BranchLine l : reverseLineMap.keySet()){
			if (l.decompNum != -1){
				String tickMark = "\u221A" + generateSubscript(l.decompNum);
				JTextField field = reverseLineMap.get(l);
				Point p = field.getLocation();
				p.setLocation((p.getX()+field.getWidth()+10), (p.getY()+(field.getHeight()/2)+7));
				drawStringAt(g2d, p, tickMark);
			}
		}



		if (root.getBranches().size() > 0)
			drawBranching(root, g2d);
	}

	private void drawStringAt(Graphics2D g2d, Point p, String toDraw) {
		FontMetrics fm = g2d.getFontMetrics();

		int centerX = p.x;
		int bottomY = p.y;

		int textX = centerX - fm.stringWidth(toDraw) / 2;
		int textY = bottomY - fm.getDescent() - fm.getLeading();

		g2d.drawString(toDraw, textX, textY);
	}

	/**
	 * Gets the root of the tree
	 * 
	 * @return the Branch that is root of the tree
	 */
	public Branch getRootBranch() {
		return root;
	}

	/**
	 * Sets the root of the tree
	 * 
	 * @param newRoot the Branch that is to be the new root of the tree
	 */
	public void setRoot(Branch newRoot) {
		root = newRoot;
		root.setFontMetrics(this.getFontMetrics(this.getFont()));
		root.getWidth();
	}

	/**
	 * Removes a line from the tree
	 * 
	 * @param removedLine The line to remove
	 */
	private void removeLine(BranchLine removedLine) {
		recordState();
		BranchLine decomposedFrom = removedLine.getDecomposedFrom();
		if (decomposedFrom != null) {
			toggleSelected(removedLine, decomposedFrom.getSelectedLines());
		}
		if (!(removedLine instanceof BranchTerminator))
			for (BranchLine curLine : removedLine.getSelectedLines())
				curLine.setDecomposedFrom(null);
		int removeIndex = -1;
		for (int i = 0; i < removedLine.getParent().numLines(); i++) {
			if (removedLine.getParent().getLine(i) == removedLine) {
				removeIndex = i;
				break;
			}
		}
		removedLine.getParent().removeLine(removeIndex);
		JTextField removedField = reverseLineMap.get(removedLine);
		this.remove(removedField);
		lineMap.remove(removedField);
		reverseLineMap.remove(removedLine);
	}

	/**
	 * Unselects the currently selected line, modifying the context as such
	 */
	private void deselectCurrentLine() {
		editLine = null;
		selectedBranches = null;
		selectedLines = null;
	}

	/**
	 * Deletes the currently selected line
	 */
	public void deleteCurrentLine() {
		if (editLine == null && !editLine.isPremise())
			return;
		removeLine(editLine);
		deselectCurrentLine();
		moveComponents();
		repaint();
	}

	/**
	 * Removes a branch from the tree, deleting all references and removing its
	 * children
	 * 
	 * @param b The branch to be removed
	 */
	private void deleteBranch(Branch b) {
		recordState();
		for (Branch curChild : b.getBranches())
			deleteBranch(curChild);
		for (int i = 0; i < b.numLines(); i++) {
			removeLine(b.getLine(i));
		}
		remove(addBranchMap.get(b));
		addBranchMap.remove(b);
		remove(addLineMap.get(b));
		addLineMap.remove(b);
		remove(branchMap.get(b));
		branchMap.remove(b);
		remove(terminateMap.get(b));
		terminateMap.remove(b);
		b.getRoot().removeBranch(b);
		if (b.getRoot().getBranches().size() == 0 && b.getRoot().getDecomposedFrom() != null)
		// This was the last child
		{
			b.getRoot().getDecomposedFrom().getSelectedBranches().remove(b.getRoot());
		}
	}

	/**
	 * Deletes the currently selected branch
	 * 
	 * @return : False if the current branch is the root branch, true otherwise
	 */
	public boolean deleteCurrentBranch() {
		Branch selectedBranch = editLine.getParent();
		if (selectedBranch == root || selectedBranch == premises) {
			JOptionPane.showMessageDialog(null, "Cannot delete root and premise branches!", "Delete branch error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		deleteBranch(selectedBranch);
		deselectCurrentLine();
		moveComponents();
		repaint();
		return true;
	}

	public void deleteFirstPremise() {
		removeLine(premises.getLine(0));
	}

	public void zoomIn() {
		if (zoomLevel >= 3)
			return;
		zoomLevel++;
		double ratio = zoomMultiplicationFactor;
		Font oldF = getFont();
		Font newF = oldF.deriveFont((float) (oldF.getSize2D() * ratio));
		size = size * (float) ratio;
		setFont(newF);
		for (Branch branch : addBranchMap.keySet()) {
			int numLines = branch.numLines();
			branch.width *= ratio;
			MIN_WIDTH *= ratio;
			branch.addStatement(null);
			branch.removeLine(numLines);
			
		}
		for (JTextField text : lineMap.keySet()) {
			int width = (int)(text.getWidth() * ratio);
			int height = (int)(text.getHeight() * ratio);
			text.setSize(width,height);
			text.setFont(newF);
		}
		this.setFont(this.getFont().deriveFont(size));
		this.repaint();
		moveComponents();
	}

	public void zoomOut() {
		if (zoomLevel <= -3)
			return;
		zoomLevel--;
		double ratio = ( 1.0 / zoomMultiplicationFactor );
		Font oldF = getFont();
		Font newF = oldF.deriveFont( (float)( oldF.getSize2D() * ratio )  );
		size = size * (float)ratio;
		setFont( newF );      
		for (Branch branch : addBranchMap.keySet()) {
			int numLines = branch.numLines();
			branch.width *= ratio;
			MIN_WIDTH *= ratio;
			branch.addStatement(null);
			branch.removeLine(numLines);
			
		}
		for (JTextField text : lineMap.keySet()) {
			int width = (int)(text.getWidth() * ratio);
			int height = (int)(text.getHeight() * ratio);
			text.setSize(width,height);
			text.setFont(newF);
		}
		this.setFont(this.getFont().deriveFont(size));
		this.repaint();
		moveComponents();
	}

	// intermediate function
	public void split() throws UserError {
		if (editLine != null) //TODO: change editLine to Option
			return split(editLine); //TODO: change this to void throws UserError
		throw new UserError("No statement is currently selected!");
	}

	/**
	 * Splits the selected Branchline
	 * 
	 * @param l The Branchline to be split
	 * 
	 * @return String that describes success
	 */
	public String split(final BranchLine l) {
		ButtonGroup buttons = new ButtonGroup();
		Set<String> vars = l.split();
		RadioPanel rp = new RadioPanel(vars);
		// Setting Bounds of JFrame. 
        rp.setBounds(100, 100, 220*vars.size(), 200); 
  
        // Setting Title of frame. 
        rp.setTitle("Split Window"); 

        // Setting Visible status of frame as true. 
		rp.setVisible(true); 
		rp.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent we) {
			}
		
			@Override
			public void windowActivated(WindowEvent we) {
			}

			@Override
			public void windowClosed(WindowEvent we) {
				String var = Global.var;
				Global.s1 = ExpressionParser.parseExpression(var);
				Global.s2 = ExpressionParser.parseExpression("\u00AC"+var);
				if (l.getParent() == premises){
					TreePanel.this.addBranch(root, true, true, Global.s1);
					TreePanel.this.addBranch(root, true, true, Global.s2);
				}
				else{
					TreePanel.this.addBranch(l.getParent(), true, true, Global.s1);
					TreePanel.this.addBranch(l.getParent(), true, true, Global.s2);
				}
			}
		});

		return null;
	}

	// intermediate function
	public void mark() throws UserError {
		if (editLine != null) //TODO: fix this
			return mark(editLine);
		throw new UserError("No statement is currently selected!");
	}

	// helper function
	public String generateSubscript(int i) {
		StringBuilder sb = new StringBuilder();
		for (char ch : String.valueOf(i).toCharArray()) {
			sb.append((char) ('\u2080' + (ch - '0')));
		}
		return sb.toString();
	}

	/**
	 * Adds decomposition marks to the tree
	 * 
	 * @param l The Branchline to be marked
	 * 
	 * @return String that describes success
	 */
	public String mark(final BranchLine l) {
		String tickMark = "\u221A" + generateSubscript(decompNumber);
		l.decompNum = decompNumber;
		decompNumber++;
		Graphics2D g2d = (Graphics2D)this.getGraphics();
		JTextField field = reverseLineMap.get(l);
		Point p = field.getLocation();
		p.setLocation((p.getX()+field.getWidth()+10), (p.getY()+(field.getHeight()/2)+7));
		drawStringAt(g2d, p, tickMark);
		return null;
	}
}
