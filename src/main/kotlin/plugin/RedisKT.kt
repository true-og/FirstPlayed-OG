package plugin
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import java.util.*
import kotlinx.coroutines.*;

public class RedisKT(url: String, port: Int) {
    // bike:1
    private var client: KredsClient = newClient(Endpoint(url, port))
    public fun makeQuery(queryString : String): String {
        var result: String = ""
        runBlocking {
            result = async {
                client.get(queryString) ?: "null"
            }.await()
        }
        return result
    }
}
