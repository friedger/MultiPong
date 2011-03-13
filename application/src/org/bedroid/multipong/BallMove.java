package org.bedroid.multipong;

import org.alljoyn.bus.annotation.Position;

import android.graphics.Point;

public class BallMove {

	@Position(0)
	public Point origin;
	@Position(1)
	public int originPaddleId;
	@Position(2)
	private int outgoingAngle;

	public BallMove() {// TODO: remove empty constructor?
		this.origin = new Point();
		this.outgoingAngle = 0;
		this.originPaddleId = 0;
	}

	public BallMove(Point origin, int outgoingAngle, int originPaddleId) {
		this.origin = origin;
		this.outgoingAngle = outgoingAngle;
		this.originPaddleId = originPaddleId;
	}

	public int getGlobalAngle() {
		return ((originPaddleId * 90) + outgoingAngle) % 360;
	}
}
