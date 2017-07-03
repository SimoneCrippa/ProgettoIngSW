package junittest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.junit.Test;
import it.polimi.LM39.controller.Game;
import it.polimi.LM39.model.PlayerRank;
import it.polimi.LM39.server.NetworkPlayer;
import junit.framework.TestCase;

public class TestsendFinalPoints extends TestCase{
	
	@Test
	public void testSendFinalPoints() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		TestPlayer player = new TestPlayer();
		ArrayList<NetworkPlayer> players = new ArrayList<NetworkPlayer>();
		players.add(player);
		Game game = new Game(1,players);
		
		//reflection to call a private method of Game
		Class[] cArg = new Class[1];
		cArg[0]=ArrayList.class;
		Method method = Game.class.getDeclaredMethod("sendFinalPoints", cArg);
		method.setAccessible(true);
		method.invoke(game,new ArrayList<PlayerRank>());
		assertTrue(true);
	}

}
