package org.bedroid.multipong;

import android.graphics.Point;

public class BallMove {

	public Point origin;
	public int originPaddleId;
	private int outgoingAngle;

	public BallMove(Point origin, int outgoingAngle, int originPaddleId) {
		this.origin = origin;
		this.outgoingAngle = outgoingAngle;
		this.originPaddleId = originPaddleId;
	}

	public int getGlobalAngle() {
		return ((originPaddleId * 90) + outgoingAngle) % 360;
	}
}
