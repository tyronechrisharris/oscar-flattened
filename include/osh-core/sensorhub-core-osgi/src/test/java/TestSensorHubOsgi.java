
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.sensorhub.impl.osgi.SensorHubOsgi;


public class TestSensorHubOsgi
{
    private TestSensorHubOsgi()
    {
    }
    
    
    public static void main(String[] args) throws Exception
    {
        // copy latest core bundles into autodeploy folder
        var autoDeployDir = Paths.get("bundles");
        Files.createDirectories(autoDeployDir);
        
        var version = "2.0-beta2";
        Path[] coreBundles = {
            getBundlePath("sensorhub-core", version),
            getBundlePath("sensorhub-webui-core", version),
            getBundlePath("sensorhub-service-swe", version),
            getBundlePath("sensorhub-service-consys", version),
            getBundlePath("sensorhub-datastore-h2", version),
            getBundlePath("sensorhub-utils-kryo", version),
            Paths.get("../sensorhub-core-osgi/lib/org.apache.felix.bundlerepository-2.0.10.jar")
        };
        
        for (var srcPath: coreBundles)
        {
            var destPath = autoDeployDir.resolve(srcPath.getFileName());
            Files.copy(srcPath, new FileOutputStream(destPath.toFile()));
        }
        
        SensorHubOsgi.main(new String[] {"src/test/resources/config_empty_sost.json", "storage"});
    }
    
    
    static Path getBundlePath(String name, String version)
    {
        return Paths.get("../" + name + "/build/libs/" + name + "-" + version + "-bundle.jar");
    }

}
