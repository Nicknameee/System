package spring.application.tree.data.utility.loaders;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.io.InputStreamReader;

public class PropertyResourceLoader {
    private static String getSQLScript(Resource resource) {
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can not load resource: %s", resource.getDescription()));
        }
    }

    private static ST getSQLScriptTemplate(Resource resource) {
        try {
            return new ST(FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream())));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can not load resource: %s", resource.getDescription()));
        }
    }

    public static String getSQLScript(String location) {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        return getSQLScript(resource);
    }

    public static ST getSQLScriptTemplate(String location) {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        return getSQLScriptTemplate(resource);
    }
}
