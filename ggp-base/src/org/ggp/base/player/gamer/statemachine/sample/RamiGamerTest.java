/**
 *
 */
package org.ggp.base.player.gamer.statemachine.sample;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Reilly
 *
 */
public class RamiGamerTest extends TestCase{

	private RamiGamer rami;
	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		this.rami = new RamiGamer();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testGetNameReturnString() {
		assertEquals("Rami", rami.getName());
	}

	public void testAddStep(){

	}
}
