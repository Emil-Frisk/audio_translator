import whisper
import sys
import os

def transcribe_audio(audio_path, output_path):
    try:
        # Load the Whisper model (use a smaller model for faster processing, e.g., "base")
        model = whisper.load_model("tiny")
        
        # Transcribe the audio file
        result = model.transcribe(audio_path)
        transcribed_text = result["text"]
        
        # Save the transcribed text to the output file
        with open(output_path, "w", encoding="utf-8") as f:
            f.write(transcribed_text)
        
        print(f"Transcription completed. Output saved to {output_path}")
        return 0
    except Exception as e:
        print(f"Error during transcription: {str(e)}", file=sys.stderr)
        return 1

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python transcribe.py <audio_file_path> <output_file_path>", file=sys.stderr)
        sys.exit(1)
    
    audio_file = sys.argv[1]
    output_file = sys.argv[2]
    
    if not os.path.exists(audio_file):
        print(f"Audio file not found: {audio_file}", file=sys.stderr)
        sys.exit(1)
    
    exit_code = transcribe_audio(audio_file, output_file)
    sys.exit(exit_code)