package artificial_intelligence;

import combat.CombatMove.MoveResult;
import shared.Vec2f;

public interface Controllable {

	public boolean specialCommand(String command);

	public boolean move(int direction);

	public MoveResult Attack(int x, int y);

	public MoveResult AttackPlayer(int attackindex);

	public boolean setAttack(int attack);

	public boolean getPeace();

	public void setPeace(boolean peace);

	public boolean Pathto(int x, int y, int steps);

	public boolean Pathto(int x, int y);

	public boolean FollowPath();

	public boolean HasPath();

	public int getHealth();

	public int getResolve();

	public int getValue(int index);

	public void setValue(int index, int value);

	public Vec2f getPosition();

	public void Wait();

	public int getFlag(String flagname);

	public void startConversation();

}
