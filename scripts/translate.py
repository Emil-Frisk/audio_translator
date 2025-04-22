from transformers import MarianTokenizer, MarianMTModel
import sys
import os

def append_chunk(chunks, token_count, max_tokens, current_chunk):
     # Safety check: Count the actual tokens and adjust if necessary
        if token_count > max_tokens:
            # If the chunk is too large, reduce the number of words and retry
            new_word_count = int(len(current_chunk) / 2)
            chunk_half = current_chunk[:new_word_count]
            chunk_second_half = current_chunk[new_word_count:]
            chunks.append(" ".join(chunk_half))
            chunks.append(" ".join(chunk_second_half))
        else:
            chunks.append(" ".join(current_chunk))

def estimate_tokens_per_word(text, tokenizer, sample_size=100):
    """
    Estimate the average number of tokens per word by sampling a portion of the text.
    """
    words = text.split()[:sample_size]  # Take a sample of words
    if not words:
        return 1.5  # Default estimate if text is empty
    sample_text = " ".join(words)
    tokens = tokenizer.encode(sample_text, add_special_tokens=False)
    token_count = len(tokens)
    word_count = len(words)
    return token_count / word_count if word_count > 0 else 1.5

def split_text_into_chunks(text, tokenizer, max_tokens=300):
    """
    Split the text into chunks of approximately max_tokens tokens.
    Returns a list of (id, chunk) tuples.
    """
    # Estimate tokens per word
    tokens_per_word = estimate_tokens_per_word(text, tokenizer)
    words_per_chunk = int(max_tokens / tokens_per_word)  # Approximate words per chunk

    print(f"Estimated tokens per word: {tokens_per_word} \n words per chunk {words_per_chunk}")

    # Split the text into words
    words = text.split()
    total_words = len(words)
    chunks = []
    current_chunk = []

    max_chunk_count = int(total_words / words_per_chunk)
    
    for i in range(max_chunk_count):
        chunk_i = i*words_per_chunk
        current_chunk = words[chunk_i:words_per_chunk+chunk_i]
        chunk_text = " ".join(current_chunk)

        tokens = tokenizer.encode(chunk_text, add_special_tokens=False)
        token_count = len(tokens)
        append_chunk(chunks, token_count, max_tokens, current_chunk)

    ### add remaining words to the end
    if max_chunk_count * words_per_chunk < total_words:
        token_count = len(tokens)
        start_idx = max_chunk_count * words_per_chunk
        remaining_words = words[start_idx:]
        chunk_text = " ".join(remaining_words)
        tokens = tokenizer.encode(chunk_text, add_special_tokens=False)
        if remaining_words:
            append_chunk(chunks, token_count, max_tokens, remaining_words)

    return chunks

def translate_text(text_file_path, output_path, target_lang):
    try:
        # Map the target language to the Helsinki-NLP model code
        lang_map = {
            "spanish": "es",
            "german": "de",
            "finnish": "fi"
        }
        
        # Get the target language code (e.g., "es" for Spanish)
        target_lang_code = lang_map.get(target_lang.lower())
        if not target_lang_code:
            raise ValueError(f"Unsupported target language: {target_lang}. Supported languages: {list(lang_map.keys())}")

        # Construct the model name based on the target language
        model_name = f"Helsinki-NLP/opus-mt-en-{target_lang_code}"
        print(f"Loading model: {model_name}")

        # Load the model and tokenizer
        tokenizer = MarianTokenizer.from_pretrained(model_name)
        model = MarianMTModel.from_pretrained(model_name)

        # Read the text from the input file
        with open(text_file_path, "r", encoding="utf-8") as f:
            text = f.read().strip()

        # Tokenize the input text
        ### max length 512
        chunks = split_text_into_chunks(text, tokenizer=tokenizer)
        chunk_count = len(chunks)
        translated_chunks = []
        translated_text = ""
        progress = ""

        ### knit the text back together
        for i in range(chunk_count):
            print(f"translation progess: {(i / chunk_count)*100}%")
            inputs = tokenizer(chunks[i], return_tensors="pt", padding=True)
            # Generate translation
            translated = model.generate(**inputs)
            translated_text += tokenizer.decode(translated[0], skip_special_tokens=True)
        
        # Save the translated text to the output file
        with open(output_path, "w", encoding="utf-8") as f:
            f.write(translated_text)

        print(f"Translation completed. Output saved to {output_path}")
        return 0
    except Exception as e:
        print(f"Error during translation: {str(e)}", file=sys.stderr)
        return 1

if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python translate.py <text_file_path> <output_file_path> <target_language>", file=sys.stderr)
        sys.exit(1)
    
    text_file = sys.argv[1]
    output_file = sys.argv[2]
    target_language = sys.argv[3]
    
    if not os.path.exists(text_file):
        print(f"Text file not found: {text_file}", file=sys.stderr)
        sys.exit(1)
    
    exit_code = translate_text(text_file, output_file, target_language)
    sys.exit(exit_code)