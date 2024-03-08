package plugin
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import java.util.*
import kotlinx.coroutines.*;

public class RedisKT(url: String, port: Int) {
    // bike:1
    private val databasePrefix = "firstplayedog:joindate"
    private var client: KredsClient = newClient(Endpoint(url, port))
    public fun get(queryString : String): String {
        var result: String = ""
        runBlocking {
            result = async {
                client.get(queryString) ?: "null"
            }.await()
        }
        return result
    }
    public fun registerJoinDate(uuid: String,timestamp: Long) {
        runBlocking {
            async {
                client.set(databasePrefix + uuid , timestamp.toString())
            }.await()
        }
    }
    public fun getJoinDate(uuid: String): Long {
        var result: String = ""
         runBlocking {
            result = async {
                client.get(databasePrefix + uuid) ?: "null"
            }.await()
        }

        if(result != "null" && result != "") {
            return result.toLong()
        }
        else {
            return -1;
        }
    }
}
