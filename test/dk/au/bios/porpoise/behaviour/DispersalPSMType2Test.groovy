/*
 * Copyright (C) 2017-2019 Jacob Nabe-Nielsen <jnn@bios.au.dk>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License version 2 and only version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see 
 * <https://www.gnu.org/licenses>.
 * 
 * Linking DEPONS statically or dynamically with other modules is making a combined work based on DEPONS. 
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of DEPONS give you permission to combine DEPONS 
 * with free software programs or libraries that are released under the GNU LGPL and with code included in the 
 * standard release of Repast Simphony under the Repast Suite License (or modified versions of such code, with unchanged license). 
 * You may copy and distribute such a system following the terms of the GNU GPL for DEPONS and the licenses of the 
 * other code concerned.
 * 
 * Note that people who make modified versions of DEPONS are not obligated to grant this special exception for 
 * their modified versions; it is their choice whether to do so. 
 * The GNU General Public License gives permission to release a modified version without this exception; 
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */

package dk.au.bios.porpoise.behaviour;

import static org.junit.Assert.*
import repast.simphony.context.Context
import repast.simphony.context.DefaultContext
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder
import repast.simphony.context.space.grid.GridFactoryFinder
import repast.simphony.engine.environment.RunEnvironment
import repast.simphony.engine.environment.RunState
import repast.simphony.engine.schedule.Schedule
import repast.simphony.space.continuous.BouncyBorders
import repast.simphony.space.continuous.NdPoint
import repast.simphony.space.continuous.RandomCartesianAdder
import repast.simphony.space.grid.GridBuilderParameters
import repast.simphony.space.grid.SimpleGridAdder
import spock.lang.Specification
import dk.au.bios.porpoise.Agent
import dk.au.bios.porpoise.Globals
import dk.au.bios.porpoise.Porpoise
import dk.au.bios.porpoise.behavior.DispersalFactory
import dk.au.bios.porpoise.behavior.FastRefMemTurn
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory
import dk.au.bios.porpoise.behavior.RandomSource
import dk.au.bios.porpoise.landscape.CellDataTestData

/**
 * Unit test for the PersistentSpatialMemory.
 */
class DispersalPSMType2Test extends Specification {

	private static Context<Agent> context;
	private static Schedule schedule;
	private static RandomSource random;

	def setupSpec() {
		Globals.worldWidth = 100;
		Globals.worldHeight = 100;
		DispersalFactory.setType("PSM-Type2")
		random = Mock(RandomSource)
		random.nextCrwAngle() >>> [0.0]
		random.nextCrwAngleWithM() >>> [0.0]
		random.nextStdMove() >>> [0.0]
		random.nextCrwStepLength() >>> [0.0]
		Globals.randomSource = random

		// Repast initialization
		schedule = new Schedule();
		RunEnvironment.init(schedule, null, null, true);
		context = new DefaultContext<>();
		RunState.init().setMasterContext(context);
	}

	def "Find Most Attractive MemCell"() {
		given: "A clear Persistent Spatial Memory"
		def factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		def space = factory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(), new BouncyBorders(), [
			Globals.worldWidth,
			Globals.worldHeight] as double[], [0.5f, 0.5f] as double[])
		def gridFactory = GridFactoryFinder.createGridFactory(null);
		def grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Agent>(new repast.simphony.space.grid.BouncyBorders(), new SimpleGridAdder<Agent>(), true, Globals.worldWidth, Globals.worldHeight));
		def cellData = CellDataTestData.getCellData();
		Globals.setCellData(cellData);
		def p = new Porpoise(space, grid, context, 1, new FastRefMemTurn())
		context.add(p);
		p.setPosition(new NdPoint(50.0, 50.0));
		p.setHeading(0.0);
		p.moveAwayFromLand();  // Weird side-effect here, updating the initial poslist

		assert p.getPosition() == new NdPoint(50.0f, 50.0f)
		assert p.getHeading() == 0.0
		assert p.getAge() == 1
		def psm = new PersistentSpatialMemory(Globals.worldWidth, Globals.worldHeight, 10);


		when: "the PSM is deactivated"
		p.getDispersalBehaviour().deactivate();
		then: "the PSM is not active"
		!p.getDispersalBehaviour().isDispersing();

		when: "the PSM is activated"
		p.getDispersalBehaviour().activate();
		then: "the PSM is active"
		p.getDispersalBehaviour().isDispersing();   // Not enough data in memory yet

		when: "the PSM is first updated"
		(1..20).each { it ->
			psm.updateMemory(new NdPoint((double)it * 5, 0.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 5.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 10.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 15.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 20.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 25.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 30.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 35.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 40.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 45.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 50.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 55.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 60.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 65.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 70.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 75.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 80.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 85.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 90.0), 1.0f);
			psm.updateMemory(new NdPoint((double)it * 5, 95.0), 1.0f);
		}

		psm.updateMemory(new NdPoint(50.0f, 50.0f), 100.0f);
		//		then: "the most attractive cell is 210"
		//		p.getDispersalBehaviour().findMostAttractiveMemCell() == 210

		//		when: "the PSM is activated"
		p.getDispersalBehaviour().activate();
		then: "the PSM is active"
		p.getDispersalBehaviour().isDispersing();
	}

}
