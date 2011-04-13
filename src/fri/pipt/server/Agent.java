/*
 *  AgentField - a simple capture-the-flag simulation for distributed intelligence
 *  Copyright (C) 2011 Luka Cehovin <http://vicos.fri.uni-lj.si/lukacu>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>. 
 */
package fri.pipt.server;

import fri.pipt.arena.Arena;
import fri.pipt.protocol.Message.Direction;
import fri.pipt.server.Field.Body;
import fri.pipt.server.Field.BodyPosition;
import fri.pipt.server.Field.Cell;
import fri.pipt.server.Team.Flag;
import fri.pipt.server.Team.Headquarters;
import fri.pipt.server.Team.TeamBody;

public class Agent extends TeamBody {

	public static enum Status {
		ALIVE, DEAD
	}

	private int id;

	private Direction direction = Direction.NONE;

	private boolean alive = true;

	private Flag flag = null;

	public Agent(Team team, int id) {

		super(Arena.TILE_AGENT, team);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void move(Field arena) {

		synchronized (this) {

			if (!isAlive())
				return;

			BodyPosition position = arena.getPosition(this);

			if (position == null)
				return;

			switch (direction) {
			case DOWN:
				position.setOffsetY(position.getOffsetY() + 0.1f);
				if (Math.abs(position.getOffsetY()) < 0.01f) {
					position.setOffsetY(0);
					direction = Direction.NONE;
				}
				break;
			case UP:
				position.setOffsetY(position.getOffsetY() - 0.1f);
				if (Math.abs(position.getOffsetY()) < 0.01f) {
					position.setOffsetY(0);
					direction = Direction.NONE;
				}
				break;
			case LEFT:
				position.setOffsetX(position.getOffsetX() - 0.1f);
				if (Math.abs(position.getOffsetX()) < 0.01f) {
					position.setOffsetX(0);
					direction = Direction.NONE;
				}
				break;
			case RIGHT:
				position.setOffsetX(position.getOffsetX() + 0.1f);
				if (Math.abs(position.getOffsetX()) < 0.01f) {
					position.setOffsetX(0);
					direction = Direction.NONE;
				}
				break;
			default:
				break;
			}

			// System.out.printf("%.1f %.1f %s\n", position.getOffsetX(),
			// position.getOffsetY(), direction);

			if (!arena.putBody(this, position)) {

				Cell c = arena.getCell(position.getX(), position.getY());

				if (c != null) {

					// TODO: kaj se zgodi, ko se zaletita agenta, ki imata oba zastavico!?
					
					Body b = c.getBody();
					if (b instanceof Flag) {
						if (((Flag) b).getTeam() == getTeam()) {

							arena.removeBody(b);

							flag = (Flag) b;

							return;
						}
					}
					if (b instanceof Headquarters) {
						if (((Headquarters) b).getTeam() == getTeam()) {

							((Headquarters) b).putFlag(flag);
							flag = null;
						}
					}
					if (b instanceof Agent) {
						((Agent) b).die();
					}
				}
				die();
			}
		}

	}

	public void setDirection(Direction direction) {

		synchronized (this) {
			if (this.direction == Direction.NONE) {
				this.direction = direction;
			}

			if ((this.direction == Direction.DOWN && direction == Direction.UP)
					|| (this.direction == Direction.UP && direction == Direction.DOWN)
					|| (this.direction == Direction.LEFT && direction == Direction.RIGHT)
					|| (this.direction == Direction.RIGHT && direction == Direction.LEFT)) {
				this.direction = direction;
			}

		}

	}

	public void die() {
		alive = false;
	}

	public boolean isAlive() {

		return alive;

	}

	public Direction getDirection() {
		return direction;
	}

	public Flag getFlag() {
		return flag;
	}
	
	
	public int getTile() {
		return flag == null ? Arena.TILE_AGENT : Arena.TILE_AGENT_FLAG;
	}

}
