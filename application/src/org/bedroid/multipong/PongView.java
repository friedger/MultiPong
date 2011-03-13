package org.bedroid.multipong;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PongView extends View {

	private static final int BALL_RADIUS = 5;
	private static final int PADDLE_WIDTH_HALF = 50;
	private float paddleX = -1;
	private float ballX = 50;
	private float ballY = -1;
	private final Rect paddle = new Rect();
	private final Rect ball = new Rect();
	private int paddleId = 0;

	private BallMove incomingMove = new BallMove(new Point(0, 0), 120, 2);

	private Timer timer = new Timer();

	private Paint paint = new Paint();
	private Handler handler = new Handler();

	public PongView(Context context) {
		super(context);
		init();
	}

	public PongView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PongView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (paddleX == -1) {
			paddleX = getHeight() / 2;
		}

		paddle.left = (int) paddleX - PADDLE_WIDTH_HALF;
		paddle.top = getHeight() - 25;
		paddle.right = (int) paddleX + PADDLE_WIDTH_HALF;
		paddle.bottom = getHeight() - 5;
		canvas.drawRect(paddle, paint);

		ball.left = (int) ballX - BALL_RADIUS;
		ball.top = (int) ballY - BALL_RADIUS;
		ball.right = (int) ballX + BALL_RADIUS;
		ball.bottom = (int) ballY + BALL_RADIUS;
		canvas.drawCircle(ballX, ballY, BALL_RADIUS, paint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		paddleX = Math.min(Math.max(event.getX(), PADDLE_WIDTH_HALF),
				getHeight() - PADDLE_WIDTH_HALF);
		invalidate();
		return true;
	}

	public void setIncomingMove(BallMove incomingMove) {
		this.incomingMove = incomingMove;
		Log.d("MultiPong", "New incoming move!");
	}

	private void init() {
		setKeepScreenOn(true);
		paint.setColor(Color.WHITE);

		final Runnable invalidater = new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		};

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (ballY >= getHeight()) {
					// ballX = 0;
					// ballY = 0;
					bounceBall(0);
				} else if (ball.intersect(paddle)) {
					bounceBall(0);
				}

				if (getBallDirection() == Direction.SIDEWAYS) {

				} else {
					// ballY = Math.min(ballY + 5, getHeight());
					double adjacent = 5;
					if (getBallDirection() == Direction.UP) {
						adjacent *= -1;
					}
					double opposite = adjacent
							* Math.tan(getLocalAngle() % 180);
					double hypothenuse = Math.sqrt(Math.pow(adjacent, 2)
							+ Math.pow(opposite, 2));
					double factor = hypothenuse / 5;
					ballX += opposite / factor;
					ballY += adjacent / factor;
				}
				handler.post(invalidater);
			}
		}, new Date(), 1000 / 60);
	}

	private Direction getBallDirection() {

		if (incomingMove.getGlobalAngle() == 0
				|| incomingMove.getGlobalAngle() == 180) {
			return Direction.SIDEWAYS;
		}

		return getLocalAngle() < 180 ? Direction.UP : Direction.DOWN;
	}

	private int getLocalAngle() {
		return ((paddleId * 90) + incomingMove.getGlobalAngle()) % 360;
	}

	private void bounceBall(int skew) {
		int localAngle = getLocalAngle();
		int outgoingAngle = 180 - localAngle;
		Point origin = new Point((int) ballX, (int) ballY);
		incomingMove = new BallMove(origin, outgoingAngle, paddleId);
	}

	private static enum Direction {
		UP, DOWN, SIDEWAYS;
	}
}
