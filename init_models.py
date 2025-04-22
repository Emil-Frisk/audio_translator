#!/usr/bin/env python3
import sys
import whisper
from transformers import MarianTokenizer, MarianMTModel

def preload_models():
    try:
        lang_map = {
            "spanish": "es",
            "german": "de",
            "finnish": "fi"
        }

        # Iterate over each language to download its model and tokenizer
        for lang, code in lang_map.items():
            model_name = f"Helsinki-NLP/opus-mt-en-{code}"
            print(f"Pre-downloading model: {model_name}")
            
            # Load tokenizer and model to trigger download
            tokenizer = MarianTokenizer.from_pretrained(model_name)
            model = MarianMTModel.from_pretrained(model_name)
            
            print(f"Successfully downloaded {model_name}")

        model = whisper.load_model("tiny")
        print(f"Successfully downloaded tiny whisper model")

        print("All models pre-downloaded successfully")
        return 0
    except Exception as e:
        print(f"Error pre-downloading models: {str(e)}", file=sys.stderr)
        return 1

if __name__ == "__main__":
    sys.exit(preload_models())