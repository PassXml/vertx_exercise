import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.future.FutureKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.start2do.tcp.TCPVerticle;
import org.start2do.vertx.Deployment;
import org.start2do.vertx.Top;
import org.start2do.vertx.VertxRunner;
import org.start2do.vertx.pojo.ServiceVerticle;

/**
 * @Author Lijie
 *
 * @date 2021/9/11:15:19
 */
@lombok.Getter
@lombok.Setter
public class Mains {
  public static void main(String[] args) {
    VertxRunner.run(new Deployment(Run2.class));
  }

  @ServiceVerticle
  public static class Run2 extends TCPVerticle {

    @Override
    public void closeHandle(@NotNull NetSocket sock) {

      getLogger().info("{}断开连接", sock.writeHandlerID());
    }

    @Override
    public void handler(@NotNull Buffer buffer, @NotNull NetSocket sock) {
      getLogger().info("{},发送了,{}", sock.writeHandlerID(), buffer.toString());
      BuildersKt.launch(
        GlobalScope.INSTANCE,
        Top.INSTANCE.getCCoroutineExceptionHandler(),
        CoroutineStart.DEFAULT,
        (coroutineScope, continuation) -> test(continuation));
    }
  }
}
