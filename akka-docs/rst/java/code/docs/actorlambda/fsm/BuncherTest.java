/**
 * Copyright (C) 2009-2015 Typesafe Inc. <http://www.typesafe.com>
 */

package docs.actorlambda.fsm;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.LinkedList;

import docs.actorlambda.fsm.*;
import static docs.actorlambda.fsm.Events.Batch;
import static docs.actorlambda.fsm.Events.Queue;
import static docs.actorlambda.fsm.Events.SetTarget;
import static docs.actorlambda.fsm.Events.Flush.Flush;

//#test-code
public class BuncherTest {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("BuncherTest");
  }

  @AfterClass
  public static void tearDown() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void testBuncherActorBatchesCorrectly() {
    new JavaTestKit(system) {{
      final ActorRef buncher =
        system.actorOf(Props.create(Buncher.class));
      final ActorRef probe = getRef();

      buncher.tell(new SetTarget(probe), probe);
      buncher.tell(new Queue(42), probe);
      buncher.tell(new Queue(43), probe);
      LinkedList<Object> list1 = new LinkedList<>();
      list1.add(42);
      list1.add(43);
      expectMsgEquals(new Batch(list1));
      buncher.tell(new Queue(44), probe);
      buncher.tell(Flush, probe);
      buncher.tell(new Queue(45), probe);
      LinkedList<Object> list2 = new LinkedList<>();
      list2.add(44);
      expectMsgEquals(new Batch(list2));
      LinkedList<Object> list3 = new LinkedList<>();
      list3.add(45);
      expectMsgEquals(new Batch(list3));
      system.stop(buncher);
    }};
  }

  @Test
  public void testBuncherActorDoesntBatchUninitialized() {
    new JavaTestKit(system) {{
      final ActorRef buncher =
        system.actorOf(Props.create(Buncher.class));
      final ActorRef probe = getRef();

      buncher.tell(new Queue(42), probe);
      expectNoMsg();
      system.stop(buncher);
    }};
  }
}
//#test-code
