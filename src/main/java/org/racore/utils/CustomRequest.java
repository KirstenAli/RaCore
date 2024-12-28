package org.racore.utils;

import org.apache.commons.fileupload2.core.DiskFileItem;
import java.util.Map;

public record CustomRequest(Map<String, String> formFields, Map<String, DiskFileItem> files) {
}
