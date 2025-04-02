from gtts import gTTS
import sys
import os

def split_text_into_chunks(text, max_length=4000):
    """
    Split text into chunks to respect gTTS length limits (approx 5000 chars).
    Returns a list of text chunks.
    """
    char_total = len(text)
    if char_total <= max_length:
        return [text]
    
    chunks = []

    chunk_count = int(char_total / max_length)
    chars_per_chunk = int(char_total / chunk_count)

    for i in range(chunk_count):
        start_i = i*chars_per_chunk
        chunk_text = text[start_i:chars_per_chunk+start_i]
        chunks.append(" ".join(chunk_text))

    ### get the remainding text
    if chars_per_chunk * chunk_count < char_total:
        start_i = chars_per_chunk * chunk_count
        chunk_text = text[start_i:]
        chunks.append(" ".join(chunk_text))
    
    return chunks

def text_to_speech(text_file_path, output_path):
    try:
        # Read the text from the input file
        with open(text_file_path, "r", encoding="utf-8") as f:
            text = f.read().strip()
        
        if not text:
            raise ValueError("Input text file is empty")

        # Split text into manageable chunks (gTTS has a ~5000 char limit)
        chunks = split_text_into_chunks(text)
        chunk_count = len(chunks)
        
        # Base output path without extension
        base_output = os.path.splitext(output_path)[0]
        
        # Process each chunk
        for i, chunk in enumerate(chunks):
            print(f"Processing chunk {i + 1}/{chunk_count} ({(i / chunk_count) * 100:.1f}%)")
            
            # Create TTS object
            tts = gTTS(text=chunk, lang='en', slow=False)
            
            # Save to temporary file
            temp_output = f"{base_output}_part{i + 1}.mp3" if chunk_count > 1 else output_path
            tts.save(temp_output)
            
            print(f"Saved chunk to {temp_output}")
        
        # If multiple chunks, inform user about separate files
        if chunk_count > 1:
            print(f"Text was split into {chunk_count} parts due to length. Files saved as {base_output}_partX.mp3")
        else:
            print(f"Speech synthesis completed. Output saved to {output_path}")
        
        return 0
    
    except Exception as e:
        print(f"Error during text-to-speech conversion: {str(e)}", file=sys.stderr)
        return 1

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python tts.py <text_file_path> <output_audio_path>", file=sys.stderr)
        sys.exit(1)
    
    text_file = sys.argv[1]
    output_file = sys.argv[2]
    
    if not os.path.exists(text_file):
        print(f"Text file not found: {text_file}", file=sys.stderr)
        sys.exit(1)
    
    exit_code = text_to_speech(text_file, output_file)
    sys.exit(exit_code)