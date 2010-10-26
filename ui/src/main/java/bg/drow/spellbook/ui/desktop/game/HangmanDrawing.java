package bg.drow.spellbook.ui.desktop.game;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;


/**
 * A component that displays a hangman drawing
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class HangmanDrawing extends JComponent {
    private int stage = 0;

    public void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        double leftX = 0;
        double topY = 0;
        double width = 200;
        double height = 150;
        final double centerX = leftX + width / 2;

        int tmp = 0;

        switch (tmp) {
            // draw a hang
            case 0:

                g2.draw(new Line2D.Double(leftX, topY, leftX, topY + height));
                g2.draw(new Line2D.Double(leftX, topY, centerX, topY));
                g2.draw(new Line2D.Double(centerX, topY, centerX, topY + height / 4));

                if (stage == 0) {
                    break;
                }
            case 1:

                // draw head

                Ellipse2D circle = new Ellipse2D.Double();
                circle.setFrameFromCenter(centerX, topY + height / 4 + 10, centerX - 10, topY + height / 4);
                g2.draw(circle);

                if (stage == 1) {
                    break;
                }

            case 2:
                // draw neck

                g2.draw(new Line2D.Double(centerX, topY + height / 4 + 20, centerX, topY + height / 4 + 35));

                if (stage == 2) {
                    break;
                }

            case 3:
                // draw left hand
                g2.draw(new Line2D.Double(centerX, topY + height / 4 + 35, centerX - 20, topY + height / 4 + 45));

                if (stage == 3) {
                    break;
                }

            case 4:
                // draw right hand
                g2.draw(new Line2D.Double(centerX, topY + height / 4 + 35, centerX + 20, topY + height / 4 + 45));

                if (stage == 4) {
                    break;
                }

            case 5:

                // draw torso
                g2.draw(new Line2D.Double(centerX, topY + height / 4 + 35, centerX, topY + height / 4 + 80));

                if (stage == 5) {
                    break;
                }

            case 6:

                // draw left leg
                g2.draw(new Line2D.Double(centerX, topY + height / 4 + 80, centerX - 10, topY + height / 4 + 100));

                if (stage == 6) {
                    break;
                }

            case 7:

                // draw right leg
                g2.draw(new Line2D.Double(centerX, topY + height / 4 + 80, centerX + 10, topY + height / 4 + 100));

                if (stage == 7) {
                    break;
                }
            default:
                break;
        }
    }
}

