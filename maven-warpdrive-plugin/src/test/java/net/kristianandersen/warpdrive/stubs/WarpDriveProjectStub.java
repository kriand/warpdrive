package net.kristianandersen.warpdrive.stubs;

import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.Model;
import org.apache.maven.model.Build;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 7:39:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class WarpDriveProjectStub extends MavenProjectStub {

    public WarpDriveProjectStub()
        {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            Model model;
            
            try
            {
                model = pomReader.read( ReaderFactory.newXmlReader( new File( getBasedir(), "pom.xml" ) ) );
                setModel( model );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }

            setGroupId( model.getGroupId() );
            setArtifactId( model.getArtifactId() );
            setVersion( model.getVersion() );
            setName( model.getName() );
            setUrl( model.getUrl() );
            setPackaging( model.getPackaging() );

            Build build = new Build();
            build.setFinalName( model.getArtifactId() );
            build.setDirectory( getBasedir() + "/target" );
            build.setSourceDirectory( getBasedir() + "/src/main/java" );
            build.setOutputDirectory( getBasedir() + "/target/classes" );
            build.setTestSourceDirectory( getBasedir() + "/src/test/java" );
            build.setTestOutputDirectory( getBasedir() + "/target/test-classes" );
            setBuild( build );

            List compileSourceRoots = new ArrayList();
            compileSourceRoots.add( getBasedir() + "/src/main/java" );
            setCompileSourceRoots( compileSourceRoots );

            List testCompileSourceRoots = new ArrayList();
            testCompileSourceRoots.add( getBasedir() + "/src/test/java" );
            setTestCompileSourceRoots( testCompileSourceRoots );
        }

        /** {@inheritDoc} */
        public File getBasedir()
        {
            return new File( super.getBasedir() + "/src/test/resources/unit/project-to-test/" );
        }


}
