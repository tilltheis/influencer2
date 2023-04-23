// usage example: classNames("Input", multiline && "Input--multiline")
//   produces "Input" or "Input Input--multiline" depending on the value of multiline
export default function classNames(...names: (string | boolean | undefined)[]) {
  return names.filter((x) => x).join(" ");
}
