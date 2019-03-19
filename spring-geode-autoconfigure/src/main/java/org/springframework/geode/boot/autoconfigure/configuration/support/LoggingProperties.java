/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.boot.autoconfigure.configuration.support;

/**
 * The LoggingProperties class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class LoggingProperties {

	private static final int DEFAULT_LOG_DISK_SPACE_LIMIT = 0;
	private static final int DEFAULT_LOG_FILE_SIZE_LIMIT = 0;

	private static final String DEFAULT_LOG_LEVEL = "config";

	private int logDiskSpaceLimit = DEFAULT_LOG_DISK_SPACE_LIMIT;
	private int logFileSizeLimit = DEFAULT_LOG_FILE_SIZE_LIMIT;

	private String level = DEFAULT_LOG_LEVEL;
	private String logFile;

	public String getLevel() {
		return this.level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getLogDiskSpaceLimit() {
		return this.logDiskSpaceLimit;
	}

	public void setLogDiskSpaceLimit(int logDiskSpaceLimit) {
		this.logDiskSpaceLimit = logDiskSpaceLimit;
	}

	public String getLogFile() {
		return this.logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public int getLogFileSizeLimit() {
		return this.logFileSizeLimit;
	}

	public void setLogFileSizeLimit(int logFileSizeLimit) {
		this.logFileSizeLimit = logFileSizeLimit;
	}
}