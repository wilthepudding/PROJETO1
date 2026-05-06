# Projeto1 MIDI Tapper

App Android em Kotlin para converter arquivos `.mid/.midi` em toques na tela usando um Serviço de Acessibilidade.

## O que ele faz

- Abre arquivo MIDI do celular.
- Converte notas MIDI para as teclas: `1 2 3 4 5 6 7 8 9 0 Q E R T Y U P`.
- Permite ajustar velocidade.
- Permite calibrar as coordenadas X/Y das teclas.
- Usa acessibilidade para executar toques apenas quando você aperta **Tocar em 5 segundos**.
- Gera APK automaticamente pelo GitHub Actions.

## Como gerar o APK

1. Abra a aba **Actions** do repositório.
2. Entre no workflow **Build APK**.
3. Rode manualmente em **Run workflow**, ou faça um novo commit.
4. Quando terminar, abra o run e baixe o artefato **projeto1-debug-apk**.
5. Dentro do ZIP estará o arquivo `app-debug.apk`.

## Como usar no celular

1. Instale o APK.
2. Abra o app **Projeto1 MIDI Tapper**.
3. Toque em **Abrir permissao de acessibilidade**.
4. Ative o serviço **Projeto1 MIDI Tapper**.
5. Volte ao app e toque em **Escolher MIDI**.
6. Ajuste a velocidade se quiser.
7. Ajuste X/Y se as teclas não baterem na tela.
8. Toque em **Tocar em 5 segundos** e abra rapidamente o piano.

## Mapeamento padrão

```text
C  -> 1
C# -> Q
D  -> 2
D# -> E
E  -> 3
F  -> 4
F# -> R
G  -> 5
G# -> T
A  -> 6
A# -> Y
B  -> 7
C  -> 8
C# -> U
D  -> 9
D# -> P
E  -> 0
```

## Observações importantes

- As coordenadas padrão foram estimadas pelo print enviado. Em celulares diferentes, pode precisar calibrar.
- O app não lê tela, não captura senha e não roda sozinho: ele só toca quando você inicia a música.
- Use com responsabilidade e respeite as regras do jogo/app onde for usar.
