package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.mobs.ExtendedMob;

public interface ProgressAchievement extends Achievement
{
	int getNextLevel();
	
	String inheritFrom();
	
	String nextLevelId();
	
	ExtendedMob getExtendedMob();
}
