# Use Ubuntu as base image
FROM ubuntu:22.04

# Avoid timezone prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies including Python prerequisites
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    nano \
    maven \
    software-properties-common \
    build-essential \
    zlib1g-dev \
    libncurses5-dev \
    libgdbm-dev \
    libnss3-dev \
    libssl-dev \
    libreadline-dev \
    libffi-dev \
    libbz2-dev \
    ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Download and install Python 3.11.9
RUN wget https://www.python.org/ftp/python/3.11.9/Python-3.11.9.tar.xz && \
    tar -xf Python-3.11.9.tar.xz && \
    cd Python-3.11.9 && \
    ./configure --enable-optimizations && \
    make -j$(nproc) && \
    make altinstall && \
    cd .. && \
    rm -rf Python-3.11.9 Python-3.11.9.tar.xz

# Ensure pip is installed for Python 3.11.9
RUN curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py && \
    /usr/local/bin/python3.11 get-pip.py && \
    rm get-pip.py

# Create symbolic links for python and pip
RUN ln -sf /usr/local/bin/python3.11 /usr/local/bin/python && \
    ln -sf /usr/local/bin/pip3.11 /usr/local/bin/pip

# Install openai-whisper
RUN pip install -U openai-whisper transformers sentencepiece

# Verify openai-whisper installation
RUN python -c "import whisper; print('openai-whisper imported successfully')" && \
    python -c "from transformers import MarianTokenizer, MarianMTModel; print('transformers imported successfully')"

# Copy and run model pre-download script
COPY init_models.py /app/init_models.py
RUN python /app/init_models.py && \
    rm /app/init_models.py

# Verify model cache
RUN ls -l /root/.cache/huggingface/hub || echo "Model cache directory not found"

# Download and install Oracle JDK 21
RUN wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb && \
    apt-get update && \
    apt install -y ./jdk-21_linux-x64_bin.deb && \
    rm jdk-21_linux-x64_bin.deb && \
    rm -rf /var/lib/apt/lists/*

# Set enviroment variables
ENV JAVA_HOME=/usr/lib/jvm/jdk-21.0.7-oracle-x64
ENV PATH=${JAVA_HOME}/bin:${PATH}

# Verify all installations
RUN curl --version && \
    wget --version && \
    nano --version && \
    mvn --version  && \
    java --version && \
    python --version && \
    pip --version && \
    ffmpeg -version

# Set working directory
WORKDIR /app

# Transfer project into the container
COPY src ./src
COPY scripts ./scripts

COPY pom.xml .
COPY vite.config.ts .
COPY types.d.ts .
COPY tsconfig.json .
COPY package.json .
COPY package-lock.json .

EXPOSE 8080

# Build the application when creating the image
RUN mvn clean package -Pproduction

CMD ["java", "-jar", "target/my-app-1.0-SNAPSHOT.jar"]

# docker exec -it kontin-nimi bash
# CMD ["tail", "-f", "/dev/null"]